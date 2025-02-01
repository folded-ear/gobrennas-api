package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.services.PantryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
@Component
public class PantryMutation {

    @Autowired
    private PantryItemService pantryItemService;

    public PantryItem renameItem(Long id,
                                 String name) {
        return pantryItemService.renameItem(id, name);
    }

    public PantryItem addLabel(Long id,
                               String label) {
        return pantryItemService.addLabel(id, label);
    }

    public PantryItem removeLabel(Long id,
                                  String label) {
        return pantryItemService.removeLabel(id, label);
    }

    public PantryItem setLabels(Long id,
                                Set<String> labels) {
        return pantryItemService.setLabels(id, labels);
    }

    public PantryItem addSynonym(Long id,
                                 String synonym) {
        return pantryItemService.addSynonym(id, synonym);
    }

    public PantryItem removeSynonym(Long id,
                                    String synonym) {
        return pantryItemService.removeSynonym(id, synonym);
    }

    public PantryItem setSynonyms(Long id,
                                  Set<String> synonyms) {
        return pantryItemService.setSynonyms(id, synonyms);
    }

    public PantryItem combineItems(List<Long> ids) {
        return pantryItemService.combineItems(ids);
    }

    public Deletion deleteItem(Long id) {
        return Deletion.of(pantryItemService.deleteItem(id));
    }

    public PantryItem orderForStore(Long id, Long targetId, Boolean after) {
        // need to default here, even though the mutation has a default, in case
        // a null is explicitly passed.
        if (after == null) after = true;
        return pantryItemService.orderForStore(id, targetId, after);
    }
}
