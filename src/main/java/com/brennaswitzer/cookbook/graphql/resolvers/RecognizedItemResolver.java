package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.payload.RecognitionSuggestion;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.services.ItemService;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecognizedItemResolver implements GraphQLResolver<RecognizedItem> {

    @Autowired
    private ItemService itemService;

    public List<RecognitionSuggestion> suggestions(RecognizedItem item, int count) {
        return itemService.getSuggestions(item, count);
    }

}
