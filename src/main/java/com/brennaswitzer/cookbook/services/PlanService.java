package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.AggregateIngredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.message.MutatePlanTree;
import com.brennaswitzer.cookbook.message.PlanMessage;
import com.brennaswitzer.cookbook.payload.PlanBucketInfo;
import com.brennaswitzer.cookbook.payload.PlanItemInfo;
import com.brennaswitzer.cookbook.repositories.PlanBucketRepository;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
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

    public List<PlanItem> getTreeDeltasById(Long id, Instant cutoff) {
        val plan = getPlanById(id, AccessLevel.VIEW);
        return Stream.concat(
                        getTreeById(plan).stream(),
                        plan.getTrashBinItems().stream()
                )
                .filter(t -> t.getUpdatedAt().isAfter(cutoff))
                .collect(Collectors.toList());
    }

    public PlanMessage mutateTree(List<Long> ids, Long parentId, Long afterId) {
        PlanItem parent = getPlanItemById(parentId, AccessLevel.CHANGE);
        PlanItem after = afterId == null ? null : getPlanItemById(afterId, AccessLevel.VIEW);
        for (Long id : ids) {
            PlanItem t = getPlanItemById(id, AccessLevel.CHANGE);
            parent.addChildAfter(t, after);
            after = t;
        }
        val m = new PlanMessage();
        m.setType("tree-mutation");
        m.setInfo(new MutatePlanTree(ids, parentId, afterId));
        return m;
    }

    public PlanMessage resetSubitems(Long id, List<Long> subitemIds) {
        PlanItem t = getPlanItemById(id, AccessLevel.CHANGE);
        PlanItem prev = null;
        for (Long sid : subitemIds) {
            PlanItem curr = getPlanItemById(sid);
            t.addChildAfter(curr, prev);
            prev = curr;
        }
        return buildUpdateMessage(t);
    }

    private void sendToPlan(AggregateIngredient r, PlanItem aggItem, Double scale) {
        r.getIngredients()
                .forEach(ir -> sendToPlan(ir, aggItem, scale));
    }

    private void sendToPlan(IngredientRef ref, PlanItem aggItem, Double scale) {
        if (scale == null || scale <= 0) { // nonsense!
            scale = 1d;
        }
        boolean isAggregate = ref.getIngredient() instanceof AggregateIngredient;
        if (ref.hasQuantity()) {
            ref = ref.scale(scale);
        }
        PlanItem t = new PlanItem(
                isAggregate
                        ? ref.getIngredient().getName()
                        : ref.getRaw(),
                ref.getQuantity(),
                ref.getIngredient(),
                ref.getPreparation());
        aggItem.addAggregateComponent(t);
        if (isAggregate) {
            // Subrecipes DO NOT get scaled; there's not a quantifiable
            // relationship to multiply across.
            sendToPlan((AggregateIngredient) ref.getIngredient(), t, 1d);
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
        return plan;
    }

    private void duplicateChildren(PlanItem src, PlanItem dest) {
        if (!src.hasChildren()) return;
        for (PlanItem s : src.getOrderedChildView()) {
            PlanItem d = new PlanItem(
                    s.getName(),
                    s.getQuantity(),
                    s.getIngredient(),
                    s.getPreparation()
            );
            d.setStatus(s.getStatus()); // unclear if this is good?
            dest.addChild(d);
            duplicateChildren(s, d);
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

    public PlanMessage createItem(Object id, Long parentId, Long afterId, String name) {
        PlanItem parent = getPlanItemById(parentId, AccessLevel.CHANGE);
        PlanItem after = afterId == null ? null : getPlanItemById(afterId, AccessLevel.VIEW);
        PlanItem item = itemRepo.save(new PlanItem(name).of(parent, after));
        itemService.autoRecognize(item);
        if (item.getId() == null) itemRepo.flush();
        PlanMessage m = buildCreationMessage(item);
        m.addNewId(item.getId(), id);
        return m;
    }

    public PlanMessage createBucket(Long planId, Object bucketId, String name, LocalDate date) {
        Plan plan = getPlanById(planId, AccessLevel.ADMINISTER);
        PlanBucket bucket = new PlanBucket();
        bucket.setName(name);
        bucket.setDate(date);
        bucket.setPlan(plan);
        bucket = bucketRepo.save(bucket);
        if (bucket.getId() != null) bucketRepo.flush();
        PlanMessage m = new PlanMessage();
        m.setId(bucket.getId());
        m.setType("create-bucket");
        m.setInfo(PlanBucketInfo.from(bucket));
        m.addNewId(bucket.getId(), bucketId);
        return m;
    }

    public PlanMessage updateBucket(Long planId, Long id, String name, LocalDate date) {
        getPlanById(planId, AccessLevel.ADMINISTER); // for the authorization check
        PlanBucket bucket = bucketRepo.getReferenceById(id);
        bucket.setName(name);
        bucket.setDate(date);
        PlanMessage m = new PlanMessage();
        m.setId(bucket.getId());
        m.setType("update-bucket");
        m.setInfo(PlanBucketInfo.from(bucket));
        return m;
    }

    public PlanMessage deleteBucket(Long planId, Long id) {
        Plan plan = getPlanById(planId, AccessLevel.ADMINISTER);
        PlanBucket bucket = bucketRepo.getReferenceById(id);
        plan.getBuckets().remove(bucket);
        bucketRepo.delete(bucket);
        PlanMessage m = new PlanMessage();
        m.setId(bucket.getId());
        m.setType("delete-bucket");
        return m;
    }

    public PlanMessage renameItem(Long id, String name) {
        PlanItem item = getPlanItemById(id, AccessLevel.CHANGE);
        item.setName(name);
        if (!item.hasIngredient() || !(item.getIngredient() instanceof Recipe)) {
            itemService.updateAutoRecognition(item);
        }
        return buildUpdateMessage(item);
    }

    public PlanMessage assignItemBucket(Long id, Long bucketId) {
        PlanItem item = getPlanItemById(id, AccessLevel.CHANGE);
        item.setBucket(bucketId == null
                               ? null
                               : bucketRepo.getReferenceById(bucketId));
        return buildUpdateMessage(item);
    }

    private PlanMessage buildUpdateMessage(PlanItem item) {
        PlanMessage m = new PlanMessage();
        m.setId(item.getId());
        m.setType("update");
        m.setInfo(PlanItemInfo.fromPlanItem(item));
        return m;
    }

    public PlanMessage setItemStatus(Long id, PlanItemStatus status) {
        if (PlanItemStatus.COMPLETED.equals(status) || PlanItemStatus.DELETED.equals(status)) {
            return deleteItem(id);
        }
        PlanItem item = getPlanItemById(id, AccessLevel.CHANGE);
        item.setStatus(status);
        return buildUpdateMessage(item);
    }

    public PlanMessage deleteItem(Long id) {
        val item = getPlanItemById(id, AccessLevel.CHANGE);
        item.moveToTrash();
        val m = new PlanMessage();
        m.setId(id);
        m.setType("delete");
        return m;
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

    @SuppressWarnings("UnusedReturnValue")
    public Plan deleteGrantFromPlan(Long planId, Long userId) {
        Plan plan = getPlanById(planId, AccessLevel.ADMINISTER);
        plan.getAcl().deleteGrant(userRepo.getById(userId));
        return plan;
    }

}
