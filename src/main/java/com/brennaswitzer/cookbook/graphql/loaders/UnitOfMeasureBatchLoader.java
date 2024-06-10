package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.repositories.UnitOfMeasureRepository;
import org.dataloader.BatchLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UnitOfMeasureBatchLoader implements BatchLoader<Long, UnitOfMeasure> {

    @Autowired
    private UnitOfMeasureRepository repo;

    @Override
    public CompletionStage<List<UnitOfMeasure>> load(List<Long> uomIds) {
        Map<Long, UnitOfMeasure> byId = repo.findAllById(new HashSet<>(uomIds)).stream()
                .collect(Collectors.toMap(Identified::getId,
                                          Function.identity()));
        return CompletableFuture.completedStage(
                uomIds.stream()
                        .map(byId::get)
                        .toList());
    }

}
