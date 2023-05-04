package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import lombok.val;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;

public final class PlanTestUtils {

    private PlanTestUtils() {
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

    public static String renderTree(String header, PlanItem... items) {
        StringBuilder sb = new StringBuilder("= ")
                .append(header)
                .append(' ');
        sb.append("=".repeat(Math.max(0, 80 - sb.length())));
        sb.append('\n');
        for (PlanItem t : items) {
            renderTree(sb, t, 0);
        }
        sb.append("-".repeat(80));
        sb.append('\n');
        return sb.toString();
    }

    private static void renderTree(StringBuilder sb, PlanItem it, int depth) {
        sb.append("  ".repeat(Math.max(0, depth)));
        sb.append(it.getName());
        sb.append('\n');
        for (PlanItem s : it.getChildView(PlanItem.BY_ORDER)) {
            renderTree(sb, s, depth + 1);
        }
        if (Hibernate.unproxy(it) instanceof Plan) {
            val l = (Plan) Hibernate.unproxy(it);
            if (l.hasTrash()) {
                sb.append("  ".repeat(Math.max(0, depth)));
                sb.append("[trash]");
                sb.append('\n');
                for (PlanItem s : l.getTrashBinItems()) {
                    renderTree(sb, s, depth + 1);
                }
            }
        }
    }

}
