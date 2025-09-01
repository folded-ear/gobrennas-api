package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class PantryItemUseCountBatchLoader {

    @Autowired
    private PantryItemRepository pantryItemRepository;

    @BatchMapping
    public Map<PantryItem, Long> useCount(List<PantryItem> items) {
        return pantryItemRepository.countTotalUses(items);
    }

}
