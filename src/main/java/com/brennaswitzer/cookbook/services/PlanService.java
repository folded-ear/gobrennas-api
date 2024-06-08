package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.AggregateIngredient;
import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
@Transactional
public class PlanService {

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

    public Iterable<Plan> getPlans(User owner) {
        return getPlans(owner.getId());
    }

    public Iterable<Plan> getPlans() {
        return getPlans(principalAccess.getId());
    }

    public Iterable<Plan> getPlans(Long userId) {
        User user = userRepo.getById(userId);
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
        return item;
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
        return plan;
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
        return Stream.concat(
                        getTreeById(plan).stream(),
                        plan.getTrashBinItems().stream()
                )
                .filter(t -> t.getUpdatedAt().isAfter(cutoff))
                .collect(Collectors.toList());
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
        Ingredient ingredient = (Ingredient) Hibernate.unproxy(ref.getIngredient());
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

    public void addRecipe(Long planId, Recipe r, Double scale) {
        PlanItem recipeItem = new PlanItem(r.getName(), r);
        Plan plan = getPlanById(planId, AccessLevel.CHANGE);
        plan.addChild(recipeItem);
        sendToPlan(r, recipeItem, scale);
    }

    private PlanMessage buildCreationMessage(PlanItem item) {
        PlanItem parent = item.getParent();
        PlanMessage m = new PlanMessage();
        m.setId(parent.getId());
        m.setType("create");
        List<PlanItem> tree = getTreeById(parent);
        m.setInfo(PlanItemInfo.fromPlanItems(tree));
        return m;
    }

    public Plan createPlan(String name, User owner) {
        return createPlan(name, owner.getId());
    }

    public Plan duplicatePlan(String name, Long fromId) {
        Plan plan = createPlan(name);
        Plan src = planRepo.getReferenceById(fromId);
        duplicateChildren(src, plan);
        // todo: should duplicating a plan copy buckets and grants?
        return plan;
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
        User user = userRepo.getById(ownerId);
        Plan plan = new Plan(name);
        plan.setOwner(user);
        plan.setPosition(1 + planRepo.getMaxPosition(user));
        return planRepo.save(plan);
    }

    public PlanItem createItem(Long parentId, Long afterId, String name) {
        PlanItem parent = getPlanItemById(parentId, AccessLevel.CHANGE);
        PlanItem after = afterId == null ? null : getPlanItemById(afterId, AccessLevel.VIEW);
        PlanItem item = itemRepo.save(new PlanItem(name).of(parent, after));
        itemService.autoRecognize(item);
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
        PlanBucket bucket = new PlanBucket();
        bucket.setName(name);
        bucket.setDate(date);
        bucket.setPlan(plan);
        bucket = bucketRepo.save(bucket);
        if (bucket.getId() != null) bucketRepo.flush();
        return bucket;
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

    public PlanBucket deleteBucket(Long planId, Long id) {
        Plan plan = getPlanById(planId, AccessLevel.ADMINISTER);
        PlanBucket bucket = bucketRepo.getReferenceById(id);
        plan.getBuckets().remove(bucket);
        bucketRepo.delete(bucket);
        return bucket;
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
        if (!item.hasIngredient() || !(Hibernate.unproxy(item.getIngredient()) instanceof Recipe)) {
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
        m.setInfo(PlanItemInfo.fromPlanItem(item));
        return m;
    }

    public PlanItem setItemStatus(Long id, PlanItemStatus status) {
        Instant now = Instant.now();
        return setItemStatus(id, status, now);
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
                                       Instant doneAt) {
        if (Hibernate.unproxy(item.getIngredient()) instanceof Recipe r) {
            var h = new PlannedRecipeHistory();
            h.setRecipe(r);
            h.setOwner(principalAccess.getUser());
            h.setPlanItemId(item.getId());
            h.setPlannedAt(item.getCreatedAt());
            h.setDoneAt(doneAt);
            h.setStatus(status);
            recipeHistoryRepo.save(h);
        }
        if (item.hasChildren()) {
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
        plan.getAcl().setGrant(userRepo.getById(userId), level);
        return plan;
    }

    public Plan revokeGrantFromPlan(Long planId, Long userId) {
        Plan plan = getPlanById(planId, AccessLevel.ADMINISTER);
        plan.getAcl().revokeGrant(userRepo.getById(userId));
        return plan;
    }

}
