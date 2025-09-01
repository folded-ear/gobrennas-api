package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.payload.RecognitionSuggestion;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class RecognizedItemResolver {

    @Autowired
    private ItemService itemService;

    @SchemaMapping
    public List<RecognitionSuggestion> suggestions(RecognizedItem item,
                                                   @Argument int count) {
        return itemService.getSuggestions(item, count);
    }

}
