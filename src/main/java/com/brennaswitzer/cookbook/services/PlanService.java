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
import com.brennaswitzer.cookbook.payload.RecognitionChoice;
import com.brennaswitzer.cookbook.repositories.PlanBucketRepository;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import com.brennaswitzer.cookbook.repositories.PlannedRecipeHistoryRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.permission.PlanAccess;
import com.brennaswitzer.cookbook.security.permission.PlanBucketAccess;
import com.brennaswitzer.cookbook.security.permission.PlanItemAccess;
import com.brennaswitzer.cookbook.security.permission.PlanItemStatusAccess;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.ValueUtils;
import com.google.common.annotations.VisibleForTesting;
import lombok.val;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
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
    @VisibleForTesting
    protected PlanItemRepository itemRepo;

    @Autowired
    private PlanRepository planRepo;

    @Autowired
    private PlanBucketRepository bucketRepo;

    @Autowired
    private PlannedRecipeHistoryRepository recipeHistoryRepo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private DiffService diffService;

    @Autowired
    private EnsureUserHasAPlan ensureUserHasAPlan;

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

    @PlanItemAccess(id = "#id", level = AccessLevel.VIEW)
    public PlanItem getPlanItemById(Long id) {
        return loadItem(id);
    }

    private PlanItem loadItem(Long id) {
        return Hibernate.unproxy(itemRepo.getReferenceById(id),
                                 PlanItem.class);
    }

    @PlanAccess(id = "#id", level = AccessLevel.VIEW)
    public Plan getPlanById(Long id) {
        return loadPlan(id);
    }

    private Plan loadPlan(Long id) {
        return Hibernate.unproxy(planRepo.getReferenceById(id),
                                 Plan.class);
    }

    private PlanBucket loadBucket(Long planId, Long id) {
        PlanBucket bucket = bucketRepo.getReferenceById(id);
        if (!bucket.getPlan().getId().equals(planId)) {
            throw new IllegalArgumentException(
                    "The bucket isn't part of this plan.");
        }
        return bucket;
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

    @PlanAccess(id = "#planId", level = AccessLevel.VIEW)
    public List<PlanItem> getTreeDeltasById(Long planId, Instant cutoff) {
        val plan = loadPlan(planId);
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

    @PlanItemAccess(id = "{#parentId, #afterId, #ids}", level = AccessLevel.CHANGE)
    public PlanItem mutateTree(List<Long> ids, Long parentId, Long afterId) {
        PlanItem parent = loadItem(parentId);
        PlanItem after = afterId == null ? null : loadItem(afterId);
        for (Long id : ids) {
            PlanItem t = loadItem(id);
            ensureSamePlan(t, parent);
            parent.addChildAfter(t, after);
            after = t;
        }
        return parent;
    }

    @PlanItemAccess(id = "{#id, #subitemIds}", level = AccessLevel.CHANGE)
    public PlanItem resetSubitems(Long id, List<Long> subitemIds) {
        PlanItem item = loadItem(id);
        PlanItem prev = null;
        for (Long sid : subitemIds) {
            PlanItem curr = loadItem(sid);
            ensureSamePlan(curr, item);
            item.addChildAfter(curr, prev);
            prev = curr;
        }
        return item;
    }

    private void ensureSamePlan(PlanItem item, PlanItem parent) {
        if (!item.getPlan().equals(parent.getPlan())) {
            throw new IllegalArgumentException(
                    "Cannot move an item to a parent on a different plan.");
        }
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
    @PlanAccess(id = "#planId", level = AccessLevel.CHANGE)
    public PlanItem addRecipe(Long planId, Recipe r, Double scale) {
        PlanItem recipeItem = new PlanItem(r.getName(), r);
        recipeItem.setQuantity(Quantity.count(scale));
        Plan plan = loadPlan(planId);
        plan.addChild(recipeItem);
        sendToPlan(r, recipeItem, scale);
        planRepo.flush(); // to ensure IDs are set everywhere
        return recipeItem;
    }

    public Plan createPlan(String name, User owner) {
        return createPlan(name, owner.getId());
    }

    @PlanAccess(id = "#fromId", level = AccessLevel.VIEW)
    public Plan duplicatePlan(String name, Long fromId) {
        Plan plan = createPlan(name);
        Plan src = planRepo.getReferenceById(fromId);
        duplicateChildren(src, plan);
        for (var b : src.getBuckets()
                .stream()
                .sorted(PlanBucket.BY_POSITION)
                .toList()) {
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
        plan.getColor(); // so it gets initialized
        return planRepo.save(plan);
    }

    @PlanItemAccess(id = "{#parentId, #afterId}", level = AccessLevel.CHANGE)
    public PlanItem createItem(Long parentId, Long afterId, String name) {
        PlanItem parent = loadItem(parentId);
        PlanItem after = afterId == null ? null : loadItem(afterId);
        PlanItem item = itemRepo.save(new PlanItem(name).of(parent, after));
        if (!item.isRecognitionDisallowed()) {
            itemService.autoRecognize(item);
        }
        if (item.getId() == null) itemRepo.flush();
        return item;
    }

    /** An explicit selection opts into identity-preserving recognition. */
    @PlanItemAccess(id = "{#parentId, #afterId}", level = AccessLevel.CHANGE)
    public PlanItem createItem(Long parentId, Long afterId, String name, RecognitionChoice choice) {
        Assert.notNull(choice, "Explicit ingredient recognition requires a choice");
        PlanItem parent = loadItem(parentId);
        PlanItem after = afterId == null ? null : loadItem(afterId);
        PlanItem item = new PlanItem(name);
        // Validate before attaching or saving, so a bad choice creates nothing.
        if (!item.isRecognitionDisallowed()) {
            itemService.autoRecognize(item, choice);
        }
        item = itemRepo.save(item.of(parent, after));
        if (item.getId() == null) itemRepo.flush();
        return item;
    }

    @PlanAccess(id = "#planId", level = AccessLevel.ADMINISTER)
    public PlanBucket createBucket(Long planId, String name, LocalDate date) {
        Plan plan = loadPlan(planId);
        PlanBucket bucket = new PlanBucket(plan, name, date);
        bucket = bucketRepo.save(bucket);
        if (bucket.getId() == null) bucketRepo.flush();
        return bucket;
    }

    @PlanAccess(id = "#planId", level = AccessLevel.ADMINISTER)
    public List<PlanBucket> createBuckets(Long planId, List<UnsavedBucket> buckets) {
        Plan plan = loadPlan(planId);
        List<PlanBucket> toSave = buckets.stream()
                .map(b -> new PlanBucket(plan, b.getName(), b.getDate()))
                .toList();
        return bucketRepo.saveAll(toSave);
    }

    @PlanBucketAccess(id = "#id", level = AccessLevel.ADMINISTER)
    public PlanBucket updateBucket(Long planId, Long id, String name, LocalDate date) {
        PlanBucket bucket = loadBucket(planId, id);
        bucket.setName(name);
        bucket.setDate(date);
        return bucket;
    }

    @PlanBucketAccess(id = "{#bucketId, #afterId}", level = AccessLevel.ADMINISTER)
    public Plan moveBucket(Long planId, Long bucketId, Long afterId) {
        Plan plan = loadPlan(planId);
        PlanBucket bucket = bucketRepo.getReferenceById(bucketId);
        PlanBucket after = afterId == null
                ? null
                : bucketRepo.getReferenceById(afterId);
        plan.moveBucket(bucket, after);
        return plan;
    }

    @PlanBucketAccess(id = "#bucketId", level = AccessLevel.ADMINISTER)
    public PlanBucket deleteBucket(Long planId, Long bucketId) {
        return deleteBucketInternal(planId, bucketId);
    }

    private PlanBucket deleteBucketInternal(Long planId, Long bucketId) {
        PlanBucket bucket = loadBucket(planId, bucketId);
        bucket.setPlan(null);
        return bucket;
    }

    @PlanBucketAccess(id = "#bucketIds", level = AccessLevel.ADMINISTER)
    public List<PlanBucket> deleteBuckets(Long planId, List<Long> bucketIds) {
        return bucketIds.stream()
                .map(id -> deleteBucketInternal(planId, id))
                .toList();
    }

    @PlanItemAccess(id = "#id", level = AccessLevel.CHANGE)
    public PlanItem renameItem(Long id, String name) {
        PlanItem item = loadItem(id);
        item.setName(name);
        if (item.isRecognitionDisallowed()) {
            itemService.clearAutoRecognition(item);
        } else if (!item.hasIngredient() || !(Hibernate.unproxy(item.getIngredient()) instanceof Recipe)) {
            itemService.updateAutoRecognition(item);
        }
        return item;
    }

    @PlanItemAccess(id = "#id", level = AccessLevel.CHANGE)
    public PlanItem assignItemBucket(Long id, Long bucketId) {
        PlanItem item = loadItem(id);
        PlanBucket bucket = bucketId == null
                ? null
                : bucketRepo.getReferenceById(bucketId);
        if (bucket != null && !item.getPlan().equals(bucket.getPlan())) {
            throw new IllegalArgumentException("Cannot assign item to a bucket from a different plan.");
        }
        item.setBucket(bucket);
        return item;
    }

    @PlanItemAccess(id = "#id", level = AccessLevel.CHANGE)
    public PlanItem setAssignee(Long id, Long userId) {
        PlanItem item = loadItem(id);
        if (userId == null) {
            item.setAssignee(null);
            return item;
        }
        User assignee = userRepo.getReferenceById(userId);
        if (!item.getPlan().isPermitted(assignee, AccessLevel.VIEW)) {
            throw new IllegalArgumentException(
                    "Cannot assign an item to a user without access to its plan.");
        }
        item.setAssignee(assignee);
        return item;
    }

    @PlanItemStatusAccess(id = "#id", status = "#status")
    public PlanItem setItemStatus(Long id, PlanItemStatus status) {
        return setItemStatus(id, status, null);
    }

    @PlanItemStatusAccess(id = "#id", status = "#status")
    public PlanItem setItemStatus(Long id, PlanItemStatus status, Instant doneAt) {
        PlanItem item = loadItem(id);
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
            var lines = new PrintForHistoryDiff(r, item)
                    .withRecipeScale(scale)
                    .withExtraPlanItemSink(it -> recordRecipeHistories(it, status, doneAt, 1))
                    .print();
            var diff = diffService.diffLinesToPatch(
                    lines.recipe(),
                    lines.planned());
            if (ValueUtils.hasValue(diff)) {
                h.setNotes("```diff\n" + diff + "```\n");
            }
            recipeHistoryRepo.save(h);
        } else if (item.hasChildren()) {
            item.getChildView()
                    .forEach(it -> recordRecipeHistories(it, status, doneAt, 1));
        }
    }

    @PlanItemAccess(id = "#id", level = AccessLevel.CHANGE)
    public PlanItem deleteItem(Long id) {
        return setItemStatus(id, PlanItemStatus.DELETED);
    }

    @PlanAccess(id = "#id", level = AccessLevel.ADMINISTER)
    public Plan deletePlan(Long id) {
        val plan = loadPlan(id);
        // grab this before delete, to avoid JPA state weirdness.
        User user = plan.getOwner();
        planRepo.delete(plan);
        ensureUserHasAPlan.ensurePlan(user);
        return plan;
    }

    public void severLibraryLinks(Recipe r) {
        itemRepo.findByIngredient(r).forEach(t -> {
            if (!t.hasNotes()) t.setNotes(r.getDirections());
            t.setIngredient(null);
        });
    }

    @PlanAccess(id = "#planId", level = AccessLevel.ADMINISTER)
    public Plan setGrantOnPlan(Long planId, Long userId, AccessLevel level) {
        Plan plan = loadPlan(planId);
        plan.getAcl().setGrant(userRepo.getReferenceById(userId), level);
        return plan;
    }

    @PlanAccess(id = "#planId", level = AccessLevel.ADMINISTER)
    public Plan revokeGrantFromPlan(Long planId, Long userId) {
        Plan plan = loadPlan(planId);
        plan.getAcl().revokeGrant(userRepo.getReferenceById(userId));
        itemRepo.findAllById(itemRepo.getIdsAssignedTo(planId, userId))
                .forEach(it -> it.setAssignee(null));
        return plan;
    }

    @PlanAccess(id = "#planId", level = AccessLevel.CHANGE)
    public Plan setColor(Long planId, String color) {
        if (StringUtils.hasText(color) && !RE_COLOR.matcher(color).matches()) {
            throw new IllegalArgumentException(String.format(
                    "Color '%s' is invalid. Use six hash-prefix digits (e.g., '#f57f17').",
                    color));
        }
        Plan plan = loadPlan(planId);
        plan.setColor(color);
        return plan;
    }

    @PlanAccess(id = "#planId", level = AccessLevel.CHANGE)
    public Plan updatePlanNotes(Long planId, String notes) {
        Plan plan = loadPlan(planId);
        plan.setNotes(StringUtils.hasText(notes) ? notes : null);
        return plan;
    }

}
