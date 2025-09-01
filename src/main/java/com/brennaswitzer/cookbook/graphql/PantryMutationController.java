package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.services.PantryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Set;

@Controller
public class PantryMutationController {

    record PantryMutation() {}

    @Autowired
    private PantryItemService pantryItemService;

    @MutationMapping
    PantryMutation pantry() {
        return new PantryMutation();
    }

    @SchemaMapping
    PantryItem renameItem(PantryMutation pantryMut,
                          @Argument Long id,
                          @Argument String name) {
        return pantryItemService.renameItem(id, name);
    }

    @SchemaMapping
    PantryItem addLabel(PantryMutation pantryMut,
                        @Argument Long id,
                        @Argument String label) {
        return pantryItemService.addLabel(id, label);
    }

    @SchemaMapping
    PantryItem removeLabel(PantryMutation pantryMut,
                           @Argument Long id,
                           @Argument String label) {
        return pantryItemService.removeLabel(id, label);
    }

    @SchemaMapping
    PantryItem setLabels(PantryMutation pantryMut,
                         @Argument Long id,
                         @Argument Set<String> labels) {
        return pantryItemService.setLabels(id, labels);
    }

    @SchemaMapping
    PantryItem addSynonym(PantryMutation pantryMut,
                          @Argument Long id,
                          @Argument String synonym) {
        return pantryItemService.addSynonym(id, synonym);
    }

    @SchemaMapping
    PantryItem removeSynonym(PantryMutation pantryMut,
                             @Argument Long id,
                             @Argument String synonym) {
        return pantryItemService.removeSynonym(id, synonym);
    }

    @SchemaMapping
    PantryItem setSynonyms(PantryMutation pantryMut,
                           @Argument Long id,
                           @Argument Set<String> synonyms) {
        return pantryItemService.setSynonyms(id, synonyms);
    }

    @SchemaMapping
    PantryItem combineItems(PantryMutation pantryMut,
                            @Argument List<Long> ids) {
        return pantryItemService.combineItems(ids);
    }

    @SchemaMapping
    Deletion deleteItem(PantryMutation pantryMut,
                        @Argument Long id) {
        return Deletion.of(pantryItemService.deleteItem(id));
    }

    @SchemaMapping
    PantryItem orderForStore(PantryMutation pantryMut,
                             @Argument Long id,
                             @Argument Long targetId,
                             @Argument Boolean after) {
        // need to default here, even though the mutation has a default, in case
        // a null is explicitly passed.
        if (after == null) after = true;
        return pantryItemService.orderForStore(id, targetId, after);
    }

}
