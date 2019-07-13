package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.ShoppingList;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MagicalListItemStatsComparator implements Comparator<ShoppingList.Item> {

    private Map<Long, Double> ingredientOrder;

    // todo: make this user specific
    public MagicalListItemStatsComparator(JdbcTemplate jdbcTmpl) {
        ingredientOrder = new HashMap<>();
        // todo: this should use some sort of time decay, not just raw average
        jdbcTmpl.queryForList("with raw as (\n" +
                "    select i.id\n" +
                "         , percent_rank() over (partition by shopping_list_id order by completed_at) rank\n" +
                "    from shopping_list_items li\n" +
                "             join ingredient i on li.ingredient_id = i.id\n" +
                "    where li.completed_at is not null\n" +
                ")\n" +
                "select id, avg(rank) pos\n" +
                "from raw\n" +
                "group by id;\n")
                .forEach(it -> ingredientOrder.put(
                        (Long) it.get("id"),
                        (Double) it.get("pos")));
    }

    @Override
    public int compare(ShoppingList.Item a, ShoppingList.Item b) {
        Double an = ingredientOrder.getOrDefault(a.getIngredient().getId(), 0.0);
        Double bn = ingredientOrder.getOrDefault(b.getIngredient().getId(), 0.0);
        return an.compareTo(bn);
    }

}
