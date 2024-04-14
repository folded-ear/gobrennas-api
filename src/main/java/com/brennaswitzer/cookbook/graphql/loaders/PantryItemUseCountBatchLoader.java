package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import org.dataloader.BatchLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
public class PantryItemUseCountBatchLoader implements BatchLoader<PantryItem, Long> {

    @Autowired
    private PantryItemRepository pantryItemRepository;

    public CompletionStage<List<Long>> load(List<PantryItem> items) {
        return CompletableFuture.supplyAsync(() -> items.stream()
                .map(pantryItemRepository.countTotalUses(items)::get)
                .toList());
    }

}
