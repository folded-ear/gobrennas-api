package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.AggregateIngredient;
import com.brennaswitzer.cookbook.domain.BaseEntity;
import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.graphql.model.UnsavedBucket;
import com.brennaswitzer.cookbook.repositories.PlanBucketRepository;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import com.brennaswitzer.cookbook.repositories.PlannedRecipeHistoryRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.ValueUtils;
import lombok.val;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
@Transactional
public class PlanService {

    private static final Pattern RE_COLOR = Pattern.compile("#[0-9a-fA-F]{6}");

    @Autowired
    protected PlanItemRepository itemRepo;

    @Autowired
    protected PlanRepository planRepo;

    @Autowired
    protected PlanBucketRepository bucketRepo;

    @Autowired
    private PlannedRecipeHistoryRepository recipeHistoryRepo;

    @Autowired
    protected UserPrincipalAccess principalAccess;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private DiffService diffService;

    public Iterable<Plan> getPlans(User owner) {
        return getPlans(owner.getId());
    }

    public Iterable<Plan> getPlans(Long userId) {
        User user = userRepo.getReferenceById(userId);
        List<Plan> result = new LinkedList<>();
        planRepo.findAccessiblePlans(userId)
                .forEach(l -> {
                    if (l.isPermitted(user, AccessLevel.VIEW)) {
                        result.add(l);
                    }
                });
        return result;
    }

    public PlanItem getPlanItemById(Long id) {
        return getPlanItemById(id, AccessLevel.VIEW);
    }

    public PlanItem getPlanItemById(Long id, AccessLevel requiredAccess) {
        PlanItem item = itemRepo.getReferenceById(id);
        item.getPlan().ensurePermitted(
                principalAccess.getUser(),
                requiredAccess
        );
        return Hibernate.unproxy(item, PlanItem.class);
    }

    public Plan getPlanById(Long id) {
        return getPlanById(id, AccessLevel.VIEW);
    }

    public Plan getPlanById(Long id, AccessLevel requiredAccess) {
        Plan plan = planRepo.getReferenceById(id);
        plan.ensurePermitted(
                principalAccess.getUser(),
                requiredAccess
        );
        return Hibernate.unproxy(plan, Plan.class);
    }

    public List<PlanItem> getTreeById(PlanItem item) {
        List<PlanItem> treeItems = new ArrayList<>();
        treeHelper(item, treeItems::add);
        return treeItems;
    }

    private void treeHelper(PlanItem item, Consumer<PlanItem> itemSink) {
        itemSink.accept(item);
        if (item.hasChildren()) {
            item.getOrderedChildView()
                    .forEach(t -> treeHelper(t, itemSink));
        }
    }

    public List<PlanItem> getTreeDeltasById(Long planId, Instant cutoff) {
        val plan = getPlanById(planId, AccessLevel.VIEW);
        List<PlanItem> result = itemRepo.findAllById(
                itemRepo.getUpdatedSince(planId, cutoff));
        Predicate<BaseEntity> filter = it -> it.getUpdatedAt().isAfter(cutoff);
        // bucket changes count as the plan itself
        if (!filter.test(plan) && plan.getBuckets()
                .stream()
                .anyMatch(filter)) {
            result.add(plan);
        }
        return result;
    }

    public PlanItem mutateTree(List<Long> ids, Long parentId, Long afterId) {
        PlanItem parent = getPlanItemById(parentId, AccessLevel.CHANGE);
        PlanItem after = afterId == null ? null : getPlanItemById(afterId, AccessLevel.VIEW);
        for (Long id : ids) {
            PlanItem t = getPlanItemById(id, AccessLevel.CHANGE);
            parent.addChildAfter(t, after);
            after = t;
        }
        return parent;
    }

    public PlanItem resetSubitems(Long id, List<Long> subitemIds) {
        PlanItem item = getPlanItemById(id, AccessLevel.CHANGE);
        PlanItem prev = null;
        for (Long sid : subitemIds) {
            PlanItem curr = getPlanItemById(sid);
            item.addChildAfter(curr, prev);
            prev = curr;
        }
        return item;
    }

    private void sendToPlan(AggregateIngredient r, PlanItem aggItem, Double scale) {
        r.getIngredients()
                .forEach(ir -> sendToPlan(ir, aggItem, scale));
    }

    private void sendToPlan(IngredientRef ref, PlanItem aggItem, Double scale) {
        // ignore nonsense scaling
        if (scale != null && scale > 0) {
            ref = ref.scale(scale);
        }
        Ingredient ingredient = Hibernate.unproxy(ref.getIngredient(), Ingredient.class);
        if (ingredient instanceof AggregateIngredient agg) {
            PlanItem it = new PlanItem(
                    ingredient.getName(),
                    ref.getQuantity(),
                    ingredient,
                    ref.getPreparation());
            aggItem.addAggregateComponent(it);
            // Subrecipes DO NOT get scaled; there's not a quantifiable
            // relationship to multiply across. The ref's quantity itself is
            // scaled, so the parent recipe remains intact. Sections, however,
            // DO get scaled as they are "part of" the parent recipe (and never
            // have a quantity on their ref).
            if (!agg.isOwnedSection()) {
                scale = 1d;
            }
            sendToPlan(agg, it, scale);
        } else {
            aggItem.addAggregateComponent(new PlanItem(
                    ref.toString(),
                    ref.getQuantity(),
                    ingredient,
                    ref.getPreparation()));
        }
    }

    /**
     * I add the passed Recipe to the specified plan, and return the new PlanItem
     * corresponding to the recipe itself.
     */
    public PlanItem addRecipe(Long planId, Recipe r, Double scale) {
        PlanItem recipeItem = new PlanItem(r.getName(), r);
        recipeItem.setQuantity(Quantity.count(scale));
        Plan plan = getPlanById(planId, AccessLevel.CHANGE);
        plan.addChild(recipeItem);
        sendToPlan(r, recipeItem, scale);
        planRepo.flush(); // to ensure IDs are set everywhere
        return recipeItem;
    }

    public Plan createPlan(String name, User owner) {
        return createPlan(name, owner.getId());
    }

    public Plan duplicatePlan(String name, Long fromId) {
        Plan plan = createPlan(name);
        Plan src = planRepo.getReferenceById(fromId);
        duplicateChildren(src, plan);
        for (var b : src.getBuckets()) {
            new PlanBucket(plan, b.getName(), b.getDate());
        }
        // todo: should duplicating a plan include grants?
        return planRepo.save(plan);
    }

    private void duplicateChildren(PlanItem src, PlanItem dest) {
        Map<PlanItem, PlanItem> srcToDest = new HashMap<>();
        duplicateChildren(src, dest, srcToDest);
        // When a component is moved out from under its aggregate, it may move
        // earlier on the plan, so have to do these as a second pass.
        srcToDest.forEach((s, d) -> {
            if (s.isAggregated()) {
                d.setAggregate(srcToDest.get(s.getAggregate()));
            }
        });
    }

    private void duplicateChildren(PlanItem src, PlanItem dest, Map<PlanItem, PlanItem> srcToDest) {
        srcToDest.put(src, dest);
        if (!src.hasChildren()) return;
        for (PlanItem s : src.getOrderedChildView()) {
            PlanItem d = new PlanItem(
                    s.getName(),
                    s.getQuantity(),
                    s.getIngredient(),
                    s.getPreparation()
            );
            d.setStatus(s.getStatus());
            d.setNotes(s.getNotes());
            dest.addChild(d);
            duplicateChildren(s, d, srcToDest);
        }
    }

    public Plan createPlan(String name) {
        return createPlan(name, principalAccess.getId());
    }

    public Plan createPlan(String name, Long ownerId) {
        User user = userRepo.getReferenceById(ownerId);
        Plan plan = new Plan(name);
        plan.setOwner(user);
        plan.setPosition(1 + planRepo.getMaxPosition(user));
        return planRepo.save(plan);
    }

    public PlanItem createItem(Long parentId, Long afterId, String name) {
        PlanItem parent = getPlanItemById(parentId, AccessLevel.CHANGE);
        PlanItem after = afterId == null ? null : getPlanItemById(afterId, AccessLevel.VIEW);
        PlanItem item = itemRepo.save(new PlanItem(name).of(parent, after));
        if (!item.isRecognitionDisallowed()) {
            itemService.autoRecognize(item);
        }
        if (item.getId() == null) itemRepo.flush();
        return item;
    }

    public PlanBucket createBucket(Long planId, String name, LocalDate date) {
        Plan plan = getPlanById(planId, AccessLevel.ADMINISTER);
        PlanBucket bucket = new PlanBucket(plan, name, date);
        bucket = bucketRepo.save(bucket);
        if (bucket.getId() == null) bucketRepo.flush();
        return bucket;
    }

    public List<PlanBucket> createBuckets(Long planId, List<UnsavedBucket> buckets) {
        Plan plan = getPlanById(planId, AccessLevel.ADMINISTER);
        List<PlanBucket> toSave = buckets.stream()
                .map(b -> new PlanBucket(plan, b.getName(), b.getDate()))
                .toList();
        return bucketRepo.saveAll(toSave);
    }

    public PlanBucket updateBucket(Long planId, Long id, String name, LocalDate date) {
        getPlanById(planId, AccessLevel.ADMINISTER); // for the authorization check
        PlanBucket bucket = bucketRepo.getReferenceById(id);
        bucket.setName(name);
        bucket.setDate(date);
        return bucket;
    }

    public PlanBucket deleteBucket(Long planId, Long bucketId) {
        getPlanById(planId, AccessLevel.ADMINISTER);
        return deleteBucketInternal(bucketId);
    }

    private PlanBucket deleteBucketInternal(Long bucketId) {
        PlanBucket bucket = bucketRepo.getReferenceById(bucketId);
        bucket.setPlan(null);
        return bucket;
    }

    public List<PlanBucket> deleteBuckets(Long planId, List<Long> bucketIds) {
        getPlanById(planId, AccessLevel.ADMINISTER);
        return bucketIds.stream()
                .map(this::deleteBucketInternal)
                .toList();
    }

    public PlanItem renameItem(Long id, String name) {
        PlanItem item = getPlanItemById(id, AccessLevel.CHANGE);
        item.setName(name);
        if (item.isRecognitionDisallowed()) {
            itemService.clearAutoRecognition(item);
        } else if (!item.hasIngredient() || !(Hibernate.unproxy(item.getIngredient()) instanceof Recipe)) {
            itemService.updateAutoRecognition(item);
        }
        return item;
    }

    public PlanItem assignItemBucket(Long id, Long bucketId) {
        PlanItem item = getPlanItemById(id, AccessLevel.CHANGE);
        PlanBucket bucket = bucketId == null
                ? null
                : bucketRepo.getReferenceById(bucketId);
        if (bucket != null && !item.getPlan().equals(bucket.getPlan())) {
            throw new IllegalArgumentException("Cannot assign item to a bucket from a different plan.");
        }
        item.setBucket(bucket);
        return item;
    }

    public PlanItem setItemStatus(Long id, PlanItemStatus status) {
        return setItemStatus(id, status, null);
    }

    public PlanItem setItemStatus(Long id, PlanItemStatus status, Instant doneAt) {
        PlanItem item = getPlanItemById(id, AccessLevel.CHANGE);
        item.setStatus(status);
        if (item.getStatus().isForDelete()) {
            double scale = item.hasQuantity()
                    ? item.getQuantity().getQuantity()
                    : 1;
            recordRecipeHistories(item, item.getStatus(), doneAt, scale);
            item.moveToTrash();
        }
        return item;
    }

    private void recordRecipeHistories(PlanItem item,
                                       PlanItemStatus status,
                                       Instant doneAtOrNull,
                                       double scale) {
        Instant doneAt = Optional.ofNullable(doneAtOrNull)
                .orElseGet(Instant::now);
        if (Hibernate.unproxy(item.getIngredient()) instanceof Recipe r) {
            if (status == PlanItemStatus.DELETED
                && Duration.between(item.getCreatedAt(), doneAt).toMinutes() < 120) {
                // If deleted within two hours of adding, don't record history.
                // It was probably tentatively added and then decided against.
                return;
            }
            var h = new PlannedRecipeHistory();
            h.setRecipe(r);
            h.setOwner(principalAccess.getUser());
            h.setPlanItemId(item.getId());
            h.setPlannedAt(item.getCreatedAt());
            h.setDoneAt(doneAt);
            h.setStatus(status);
            var recipeLines = new ArrayList<String>();
            recipeLines.add(r.getName());
            recipeLines.add("");
            r.getIngredients()
                    .forEach(ir -> recipeLines.add(ir.scale(scale).toRaw(true)));
            var planLines = new ArrayList<String>();
            planLines.add(item.getName());
            planLines.add("");
            item.getOrderedChildView()
                    .forEach(it -> {
                        planLines.add(it.toRaw(true));
                        recordRecipeHistories(it, status, doneAt, 1);
                    });
            var diff = diffService.diffLinesToPatch(recipeLines, planLines);
            if (ValueUtils.hasValue(diff)) {
                h.setNotes("```diff\n" + diff + "```\n");
            }
            recipeHistoryRepo.save(h);
        } else if (item.hasChildren()) {
            item.getChildView()
                    .forEach(it -> recordRecipeHistories(it, status, doneAt, 1));
        }
    }

    public PlanItem deleteItem(Long id) {
        return setItemStatus(id, PlanItemStatus.DELETED);
    }

    public Plan deletePlan(Long id) {
        val plan = getPlanById(id, AccessLevel.ADMINISTER);
        planRepo.delete(plan);
        return plan;
    }

    public void severLibraryLinks(Recipe r) {
        itemRepo.findByIngredient(r).forEach(t -> {
            if (!t.hasNotes()) t.setNotes(r.getDirections());
            t.setIngredient(null);
        });
    }

    public Plan setGrantOnPlan(Long planId, Long userId, AccessLevel level) {
        Plan plan = getPlanById(planId, AccessLevel.ADMINISTER);
        plan.getAcl().setGrant(userRepo.getReferenceById(userId), level);
        return plan;
    }

    public Plan revokeGrantFromPlan(Long planId, Long userId) {
        Plan plan = getPlanById(planId, AccessLevel.ADMINISTER);
        plan.getAcl().revokeGrant(userRepo.getReferenceById(userId));
        return plan;
    }

    public Plan setColor(Long planId, String color) {
        if (StringUtils.hasText(color) && !RE_COLOR.matcher(color).matches()) {
            throw new IllegalArgumentException(String.format(
                    "Color '%s' is invalid. Use six hash-prefix digits (e.g., '#f57f17').",
                    color));
        }
        Plan plan = getPlanById(planId, AccessLevel.CHANGE);
        plan.setColor(color);
        return plan;
    }

    public Plan updatePlanNotes(Long planId, String notes) {
        Plan plan = getPlanById(planId, AccessLevel.CHANGE);
        plan.setNotes(StringUtils.hasText(notes) ? notes : null);
        return plan;
    }

}
