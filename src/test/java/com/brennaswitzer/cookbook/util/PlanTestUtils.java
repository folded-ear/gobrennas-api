package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;

public final class PlanTestUtils {

    private PlanTestUtils() {
    }

    public static <T extends PlanItem> void printTree(String header, Iterable<T> items) {
        System.out.println(renderTree(header, items));
    }

    public static <T extends PlanItem> String renderTree(String header, Iterable<T> items) {
        List<T> itemList;
        if (items instanceof List) {
            itemList = (List<T>) items;
        } else {
            itemList = new ArrayList<>();
            for (T t : items) {
                itemList.add(t);
            }
        }
        return renderTree(header, itemList.toArray(new PlanItem[0]));
    }

    public static void printTree(String header, PlanItem... items) {
        System.out.println(renderTree(header, items));
    }

    public static String renderTree(String header, PlanItem... items) {
        StringBuilder sb = new StringBuilder("= ")
                .append(header)
                .append(' ');
        sb.append("=".repeat(Math.max(1, 80 - sb.length())))
                .append('\n');
        for (PlanItem t : items) {
            renderTree(sb, t, 0);
        }
        return sb.append("-".repeat(80))
                .append('\n')
                .toString();
    }

    private static void renderTree(StringBuilder sb, PlanItem it, int depth) {
        assert depth >= 0;
        sb.append("  ".repeat(depth))
                .append(it.getName());
        if (it.isAggregated() && !it.getAggregate().equals(it.getParent())) {
            sb.append(" (of ")
                    .append(it.getAggregate().getName())
                    .append(")");
        }
        sb.append('\n');
        for (PlanItem c : it.getChildView(PlanItem.BY_ORDER)) {
            renderTree(sb, c, depth + 1);
        }
        if (Hibernate.unproxy(it) instanceof Plan p) {
            if (p.hasTrash()) {
                sb.append("  ".repeat(depth))
                        .append("[trash]")
                        .append('\n');
                for (PlanItem s : p.getTrashBinItems()) {
                    renderTree(sb, s, depth + 1);
                }
            }
        }
    }

}
