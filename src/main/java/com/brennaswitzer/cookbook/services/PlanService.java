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
import com.brennaswitzer.cookbook.message.MutatePlanTree;
import com.brennaswitzer.cookbook.message.PlanMessage;
import com.brennaswitzer.cookbook.payload.PlanBucketInfo;
import com.brennaswitzer.cookbook.payload.PlanItemInfo;
import com.brennaswitzer.cookbook.repositories.PlanBucketRepository;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import com.brennaswitzer.cookbook.repositories.PlannedRecipeHistoryRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import lombok.val;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    public Iterable<Plan> getPlans() {
        return getPlans(principalAccess.getId());
    }

    public Iterable<Plan> getPlans(Long userId) {
        User user = userRepo.getReferenceById(userId);
        List<Plan> result = new LinkedList<>();
        planRepo.findAccessibleLists(userId)
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

    public List<PlanItem> getTreeById(Long id) {
        return getTreeById(getPlanItemById(id, AccessLevel.VIEW));
    }

    public List<PlanItem> getTreeById(PlanItem item) {
        List<PlanItem> treeItems = treeHelper(item);
        for (PlanItem t : treeItems) {
            // to load the collection before the session closes
            t.getOrderedComponentsView();
        }
        return treeItems;
    }

    private List<PlanItem> treeHelper(PlanItem item) {
        List<PlanItem> treeItems = new LinkedList<>();
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
        List<PlanItem> result = new ArrayList<>();
        Predicate<BaseEntity> filter = it -> it.getUpdatedAt().isAfter(cutoff);
        // plan or any descendants
        getTreeById(plan).stream()
                .filter(filter)
                .forEach(result::add);
        // anything in the trash (deleted or completed)
        plan.getTrashBinItems().stream()
                .filter(filter)
                .forEach(result::add);
        // bucket changes count as the plan itself
        if (!filter.test(plan) && plan.getBuckets()
                .stream()
                .anyMatch(filter))
            result.add(plan);
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

    public PlanMessage mutateTreeForMessage(List<Long> ids, Long parentId, Long afterId) {
        mutateTree(ids, parentId, afterId);
        val m = new PlanMessage();
        m.setType("tree-mutation");
        m.setInfo(new MutatePlanTree(ids, parentId, afterId));
        return m;
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

    public PlanMessage resetSubitemsForMessage(Long id, List<Long> subitemIds) {
        PlanItem item = resetSubitems(id, subitemIds);
        return buildUpdateMessage(item);
    }

    private void sendToPlan(AggregateIngredient r, PlanItem aggItem, Double scale) {
        r.getIngredients()
                .forEach(ir -> sendToPlan(ir, aggItem, scale));
    }

    private void sendToPlan(IngredientRef ref, PlanItem aggItem, Double scale) {
        if (scale == null || scale <= 0) { // nonsense!
            scale = 1d;
        }
        Ingredient ingredient = Hibernate.unproxy(ref.getIngredient(), Ingredient.class);
        boolean isAggregate = ingredient instanceof AggregateIngredient;
        if (ref.hasQuantity()) {
            ref = ref.scale(scale);
        }
        PlanItem t = new PlanItem(
                isAggregate
                        ? ingredient.getName()
                        : ref.getRaw(),
                ref.getQuantity(),
                ingredient,
                ref.getPreparation());
        aggItem.addAggregateComponent(t);
        if (isAggregate) {
            // Subrecipes DO NOT get scaled; there's not a quantifiable
            // relationship to multiply across.
            sendToPlan((AggregateIngredient) ingredient, t, 1d);
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

    private PlanMessage buildCreationMessage(PlanItem item) {
        PlanItem parent = item.getParent();
        PlanMessage m = new PlanMessage();
        m.setId(parent.getId());
        m.setType("create");
        List<PlanItem> tree = getTreeById(parent);
        m.setInfo(PlanItemInfo.from(tree));
        return m;
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

    public PlanMessage createItemForMessage(Object id, Long parentId, Long afterId, String name) {
        PlanItem item = createItem(parentId, afterId, name);
        PlanMessage m = buildCreationMessage(item);
        m.addNewId(item.getId(), id);
        return m;
    }

    public PlanMessage createBucketForMessage(Long planId, Object bucketId, String name, LocalDate date) {
        PlanBucket bucket = createBucket(planId, name, date);
        PlanMessage m = new PlanMessage();
        m.setId(bucket.getId());
        m.setType("create-bucket");
        m.setInfo(PlanBucketInfo.from(bucket));
        m.addNewId(bucket.getId(), bucketId);
        return m;
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

    public PlanMessage updateBucketForMessage(Long planId, Long id, String name, LocalDate date) {
        PlanBucket bucket = updateBucket(planId, id, name, date);
        PlanMessage m = new PlanMessage();
        m.setId(bucket.getId());
        m.setType("update-bucket");
        m.setInfo(PlanBucketInfo.from(bucket));
        return m;
    }

    public PlanBucket deleteBucket(Long planId, Long bucketId) {
        getPlanById(planId, AccessLevel.ADMINISTER);
        return deleteBucketInternal(bucketId);
    }

    private @NotNull PlanBucket deleteBucketInternal(Long bucketId) {
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

    public PlanMessage deleteBucketForMessage(Long planId, Long id) {
        PlanBucket bucket = deleteBucket(planId, id);
        PlanMessage m = new PlanMessage();
        m.setId(bucket.getId());
        m.setType("delete-bucket");
        return m;
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

    public PlanMessage renameItemForMessage(Long id, String name) {
        return buildUpdateMessage(renameItem(id, name));
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

    public PlanMessage assignItemBucketForMessage(Long id, Long bucketId) {
        return buildUpdateMessage(assignItemBucket(id, bucketId));
    }

    private PlanMessage buildUpdateMessage(PlanItem item) {
        PlanMessage m = new PlanMessage();
        m.setId(item.getId());
        m.setType("update");
        m.setInfo(PlanItemInfo.from(item));
        return m;
    }

    public PlanItem setItemStatus(Long id, PlanItemStatus status) {
        return setItemStatus(id, status, null);
    }

    public PlanItem setItemStatus(Long id, PlanItemStatus status, Instant doneAt) {
        PlanItem item = getPlanItemById(id, AccessLevel.CHANGE);
        item.setStatus(status);
        if (item.getStatus().isForDelete()) {
            recordRecipeHistories(item, item.getStatus(), doneAt);
            item.moveToTrash();
        }
        return item;
    }

    private void recordRecipeHistories(PlanItem item,
                                       PlanItemStatus status,
                                       Instant doneAtOrNull) {
        Instant doneAt = doneAtOrNull == null ? Instant.now() : doneAtOrNull;
        if (Hibernate.unproxy(item.getIngredient()) instanceof Recipe r) {
            double scale = item.getQuantity().getQuantity();
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
                    .forEach(ir -> {
                        if (scale > 0 && scale != 1) {
                            ir = ir.scale(scale);
                        }
                        recipeLines.add(ir.getRaw());
                    });
            var planLines = new ArrayList<String>();
            planLines.add(item.getName());
            planLines.add("");
            item.getOrderedChildView()
                    .forEach(it -> {
                        planLines.add(it.getName());
                        recordRecipeHistories(it, status, doneAt);
                    });
            var diff = diffService.diffLinesToPatch(recipeLines, planLines);
            if (!diff.isBlank()) {
                h.setNotes("```diff\n" + diff + "\n```\n");
            }
            recipeHistoryRepo.save(h);
        } else if (item.hasChildren()) {
            item.getChildView()
                    .forEach(it -> recordRecipeHistories(it, status, doneAt));
        }
    }

    public PlanMessage setItemStatusForMessage(Long id, PlanItemStatus status) {
        PlanItem item = setItemStatus(id, status);
        if (item.getStatus().isForDelete()) {
            val m = new PlanMessage();
            m.setId(id);
            m.setType("delete");
            return m;
        }
        return buildUpdateMessage(item);
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
        Plan plan = getPlanById(planId, AccessLevel.ADMINISTER);
        plan.setColor(color);
        return plan;
    }

}
