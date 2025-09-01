package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.graphql.loaders.UnitOfMeasureBatchLoader;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public class QuantityResolver {

    @SchemaMapping
    public CompletableFuture<UnitOfMeasure> units(Quantity q,
                                                  DataFetchingEnvironment env) {
        UnitOfMeasure uom = q.getUnits();
        return env.<Long, UnitOfMeasure>getDataLoader(UnitOfMeasureBatchLoader.class.getName())
                .load(uom == null ? null : uom.getId());
    }

}
