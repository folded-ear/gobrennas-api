package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.TaskList;
import lombok.val;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;

public final class TaskTestUtils {

    private TaskTestUtils() {}

    public static <T extends PlanItem> String renderTree(String header, Iterable<T> tasks) {
        List<T> list;
        if (tasks instanceof List) {
            list = (List<T>) tasks;
        } else {
            list = new ArrayList<>();
            for (T t : tasks) {
                list.add(t);
            }
        }
        return renderTree(header, list.toArray(new PlanItem[0]));
    }

    public static String renderTree(String header, PlanItem... task) {
        StringBuilder sb = new StringBuilder("= ")
                .append(header)
                .append(' ');
        sb.append("=".repeat(Math.max(0, 80 - sb.length())));
        sb.append('\n');
        for (PlanItem t : task) {
            renderTree(sb, t, 0);
        }
        sb.append("-".repeat(80));
        sb.append('\n');
        return sb.toString();
    }

    private static void renderTree(StringBuilder sb, PlanItem t, int depth) {
        sb.append("  ".repeat(Math.max(0, depth)));
        sb.append(t.getName());
        sb.append('\n');
        for (PlanItem s : t.getSubtaskView(PlanItem.BY_ORDER)) {
            renderTree(sb, s, depth + 1);
        }
        if (Hibernate.unproxy(t) instanceof TaskList) {
            val l = (TaskList) Hibernate.unproxy(t);
            if (l.hasTrash()) {
                sb.append("  ".repeat(Math.max(0, depth)));
                sb.append("[trash]");
                sb.append('\n');
                for (PlanItem s : l.getTrashBinTasks()) {
                    renderTree(sb, s, depth + 1);
                }
            }
        }
    }

}
