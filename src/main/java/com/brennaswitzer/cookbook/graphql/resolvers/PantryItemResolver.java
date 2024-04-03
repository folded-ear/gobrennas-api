package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchScope;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PantryItemResolver implements GraphQLResolver<PantryItem> {

    @Autowired
    private RecipeRepository recipeRepository;

    public Set<String> synonyms(PantryItem pantryItem) {
        Set<String> all = pantryItem.getSynonyms();
        String name = pantryItem.getName();
        if (all.contains(name)) {
            // This is supposed to be true already, but it's enforced softly, to
            // avoid unneeded data access. Since we have to load them, may as
            // well clean it up too.
            pantryItem.removeSynonym(name);
        }
        return all;
    }

    public Set<String> labels(PantryItem pantryItem) {
        return pantryItem.getLabels()
                .stream()
                .map(Label::getName)
                .collect(Collectors.toSet());
    }

    public long useCount(PantryItem pantryItem, LibrarySearchScope scope) {
        return LibrarySearchScope.EVERYONE.equals(scope)
                ? recipeRepository.countTotalUses(pantryItem)
                : recipeRepository.countMyUses(pantryItem);
    }

    public OffsetDateTime firstUse(PantryItem item) {
        return item.getCreatedAt()
                .atOffset(ZoneOffset.UTC);
    }

}
