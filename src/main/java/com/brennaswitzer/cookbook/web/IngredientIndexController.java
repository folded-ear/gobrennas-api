package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.services.indexing.IndexStats;
import com.brennaswitzer.cookbook.services.indexing.IngredientReindexQueueService;
import com.brennaswitzer.cookbook.services.indexing.ReindexIngredients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("api/_ingredient_index")
@PreAuthorize("hasRole('DEVELOPER')")
public class IngredientIndexController {

    @Autowired
    private IngredientReindexQueueService ingredientReindexQueueService;

    @Autowired
    private ReindexIngredients reindexIngredients;

    @GetMapping("")
    public IndexStats getIngredientIndexStats() {
        return ingredientReindexQueueService.getIndexStats();
    }

    @GetMapping("/enqueue-all")
    public Map<String, Object> enqueueAll() {
        return withElapsedAndStats("enqueued",
                                   reindexIngredients::enqueueAll);
    }

    @GetMapping("/drain-queue")
    public Map<String, Object> reindex() {
        return withElapsedAndStats("reindexed",
                                   reindexIngredients::reindexQueued);
    }

    private Map<String, Object> withElapsedAndStats(String key,
                                                    Supplier<?> supplier) {
        var sw = new StopWatch();
        sw.start();
        var result = supplier.get();
        sw.stop();
        return Map.of(
                key, result,
                "elapsed", sw.getTotalTimeMillis(),
                "stats", getIngredientIndexStats());
    }

}
