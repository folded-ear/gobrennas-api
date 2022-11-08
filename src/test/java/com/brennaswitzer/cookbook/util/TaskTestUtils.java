package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import lombok.val;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;

public final class TaskTestUtils {

    private TaskTestUtils() {}

    public static <T extends Task> String renderTree(String header, Iterable<T> tasks) {
        List<T> list;
        if (tasks instanceof List) {
            list = (List<T>) tasks;
        } else {
            list = new ArrayList<>();
            for (T t : tasks) {
                list.add(t);
            }
        }
        return renderTree(header, list.toArray(new Task[0]));
    }

    public static String renderTree(String header, Task... task) {
        StringBuilder sb = new StringBuilder("= ")
                .append(header)
                .append(' ');
        for (int i = 80 - sb.length(); i > 0; i--) sb.append('=');
        sb.append('\n');
        for (Task t : task) {
            renderTree(sb, t, 0);
        }
        for (int i = 80; i > 0; i--) sb.append('-');
        sb.append('\n');
        return sb.toString();
    }

    private static void renderTree(StringBuilder sb, Task t, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        sb.append(t.getName());
        sb.append('\n');
        for (Task s : t.getSubtaskView(Task.BY_ORDER)) {
            renderTree(sb, s, depth + 1);
        }
        if (Hibernate.unproxy(t) instanceof TaskList) {
            val l = (TaskList) Hibernate.unproxy(t);
            if (l.hasTrash()) {
                for (int i = 0; i < depth; i++) {
                    sb.append("  ");
                }
                sb.append("[trash]");
                sb.append('\n');
                for (Task s : l.getTrashBinTasks()) {
                    renderTree(sb, s, depth + 1);
                }
            }
        }
    }

}
