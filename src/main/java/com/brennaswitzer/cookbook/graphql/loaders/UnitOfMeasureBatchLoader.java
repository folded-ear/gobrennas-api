package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.repositories.UnitOfMeasureRepository;
import org.dataloader.BatchLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

@Component
public class UnitOfMeasureBatchLoader implements BatchLoader<Long, UnitOfMeasure> {

    @Autowired
    private UnitOfMeasureRepository repo;

    @Override
    public CompletionStage<List<UnitOfMeasure>> load(List<Long> uomIds) {
        return CompletableFuture.supplyAsync(() -> {
            Set<Long> nonNullIds = uomIds.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, UnitOfMeasure> byId;
            if (nonNullIds.isEmpty()) byId = emptyMap();
            else byId = repo.findAllById(nonNullIds)
                    .stream()
                    .collect(Collectors.toMap(Identified::getId,
                                              Function.identity()));
            return uomIds.stream()
                    .map(byId::get)
                    .toList();
        });
    }

}
