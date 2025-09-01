package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.services.PantryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Set;

@Controller
public class PantryMutation {

    @Autowired
    private PantryItemService pantryItemService;

    @SchemaMapping(typeName = "PantryMutation")
    public PantryItem renameItem(@Argument Long id,
                                 @Argument String name) {
        return pantryItemService.renameItem(id, name);
    }

    @SchemaMapping(typeName = "PantryMutation")
    public PantryItem addLabel(@Argument Long id,
                               @Argument String label) {
        return pantryItemService.addLabel(id, label);
    }

    @SchemaMapping(typeName = "PantryMutation")
    public PantryItem removeLabel(@Argument Long id,
                                  @Argument String label) {
        return pantryItemService.removeLabel(id, label);
    }

    @SchemaMapping(typeName = "PantryMutation")
    public PantryItem setLabels(@Argument Long id,
                                @Argument Set<String> labels) {
        return pantryItemService.setLabels(id, labels);
    }

    @SchemaMapping(typeName = "PantryMutation")
    public PantryItem addSynonym(@Argument Long id,
                                 @Argument String synonym) {
        return pantryItemService.addSynonym(id, synonym);
    }

    @SchemaMapping(typeName = "PantryMutation")
    public PantryItem removeSynonym(@Argument Long id,
                                    @Argument String synonym) {
        return pantryItemService.removeSynonym(id, synonym);
    }

    @SchemaMapping(typeName = "PantryMutation")
    public PantryItem setSynonyms(@Argument Long id,
                                  @Argument Set<String> synonyms) {
        return pantryItemService.setSynonyms(id, synonyms);
    }

    @SchemaMapping(typeName = "PantryMutation")
    public PantryItem combineItems(@Argument List<Long> ids) {
        return pantryItemService.combineItems(ids);
    }

    @SchemaMapping(typeName = "PantryMutation")
    public Deletion deleteItem(@Argument Long id) {
        return Deletion.of(pantryItemService.deleteItem(id));
    }

    @SchemaMapping(typeName = "PantryMutation")
    public PantryItem orderForStore(@Argument Long id,
                                    @Argument Long targetId,
                                    @Argument Boolean after) {
        // need to default here, even though the mutation has a default, in case
        // a null is explicitly passed.
        if (after == null) after = true;
        return pantryItemService.orderForStore(id, targetId, after);
    }
}
