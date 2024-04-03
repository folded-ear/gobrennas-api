package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.services.PantryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public PantryItem addSynonym(Long id,
                                 String synonym) {
        return pantryItemService.addSynonym(id, synonym);
    }

    public PantryItem removeSynonym(Long id,
                                    String synonym) {
        return pantryItemService.removeSynonym(id, synonym);
    }

}
