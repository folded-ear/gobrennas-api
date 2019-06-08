package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.Task;

import java.util.LinkedList;
import java.util.List;

public class TaskTestUtils {

    public static String renderTree(String header, Iterable<Task> tasks) {
        List<Task> list;
        if (tasks instanceof List) {
            list = (List<Task>) tasks;
        } else {
            list = new LinkedList<>();
            for (Task t : tasks) {
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
        if (t.isQuantityInteresting()) {
            sb.append(" (")
                    .append(t.getQuantity())
                    .append(')');
        }
        sb.append('\n');
        for (Task s : t.getSubtaskView(Task.BY_ORDER)) {
            renderTree(sb, s, depth + 1);
        }
    }

}
