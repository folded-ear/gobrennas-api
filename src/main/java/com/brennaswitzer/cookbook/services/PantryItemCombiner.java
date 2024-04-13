package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@PreAuthorize("hasRole('DEVELOPER')")
public class PantryItemCombiner {

    @Autowired
    private PantryItemRepository pantryItemRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public PantryItem combineItems(PantryItem toKeep, PantryItem other) {
        Map<String, Object> idMap = Map.of("toKeep", toKeep.getId(),
                                           "other", other.getId());
        // favorite.object_id // todo: UK violations?!
        update(new NamedParameterQuery(
                """
                update favorite
                set object_id = :toKeep
                where object_type = 'PantryItem'
                  and object_id = :other
                """,
                idMap));
        // ingredient_labels.ingredient_id
        for (var lbl : other.getLabels()) {
            toKeep.addLabel(lbl);
        }
        // inventory_item.ingredient_id // todo: UK violations?!
        update(new NamedParameterQuery(
                """
                update inventory_item
                set ingredient_id = :toKeep
                where ingredient_id = :other
                """,
                idMap));
        // pantry_item_synonyms.pantry_item_id
        toKeep.addSynonym(other.getName());
        for (var syn : other.getSynonyms()) {
            toKeep.addSynonym(syn);
        }
        // plan_item.ingredient_id
        update(new NamedParameterQuery(
                """
                update plan_item
                set ingredient_id = :toKeep
                where ingredient_id = :other
                """,
                idMap));
        // recipe_ingredients.ingredient_id
        update(new NamedParameterQuery(
                """
                update recipe_ingredients
                set ingredient_id = :toKeep
                where ingredient_id = :other
                """,
                idMap));
        pantryItemRepository.delete(other);
        return toKeep;
    }

    private int update(NamedParameterQuery query) {
        return jdbcTemplate.update(query.getStatement(),
                                   query.getParameters());
    }

}
