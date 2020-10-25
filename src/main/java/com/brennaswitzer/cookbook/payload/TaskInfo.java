package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.TaskStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("WeakerAccess")
public class TaskInfo {

    public static TaskInfo fromTask(Task task) {
        TaskInfo info = new TaskInfo();
        info.id = task.getId();
        info.name = task.getName();
        info.status = task.getStatus();
        if (task.isSubtask()) {
            info.parentId = task.getParent().getId();
        }
        if (task.hasSubtasks()) {
            info.subtaskIds = new long[task.getSubtaskCount()];
            int i = 0;
            for (Task t : task.getOrderedSubtasksView()) {
                info.subtaskIds[i++] = t.getId();
            }
        }
        if (task.hasIngredient()) {
            info.ingredientId = task.getIngredient().getId();
            Quantity q = task.getQuantity();
            info.quantity = q.getQuantity();
            if (q.hasUnits()) {
                info.uomId = q.getUnits().getId();
                info.units = q.getUnits().getName();
            }
            info.preparation = task.getPreparation();
        }
        return info;
    }

    public static TaskInfo fromList(TaskList list) {
        TaskInfo info = fromTask(list);
        info.acl = AclInfo.fromAcl(list.getAcl());
        return info;
    }

    public static List<TaskInfo> fromTasks(Iterable<Task> tasks) {
        return StreamSupport.stream(tasks.spliterator(), false)
                .map(TaskInfo::fromTask)
                .collect(Collectors.toList());
    }

    public static List<TaskInfo> fromLists(Iterable<TaskList> lists) {
        return StreamSupport.stream(lists.spliterator(), false)
                .map(TaskInfo::fromList)
                .collect(Collectors.toList());
    }

    private Long id;

    private String name;

    private TaskStatus status;

    private Long parentId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private AclInfo acl;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private long[] subtaskIds;

    private Double quantity;
    private String units;
    private Long uomId;
    private Long ingredientId;
    private String preparation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public long[] getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(long[] subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public boolean hasSubtasks() {
        return subtaskIds != null && subtaskIds.length > 0;
    }

    public AclInfo getAcl() {
        return acl;
    }

    public void setAcl(AclInfo acl) {
        this.acl = acl;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Long getUomId() {
        return uomId;
    }

    public void setUomId(Long uomId) {
        this.uomId = uomId;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

}
