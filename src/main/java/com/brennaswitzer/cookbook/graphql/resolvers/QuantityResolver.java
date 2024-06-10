package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.graphql.loaders.UnitOfMeasureBatchLoader;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class QuantityResolver implements GraphQLResolver<Quantity> {

    public CompletableFuture<UnitOfMeasure> units(Quantity q,
                                                  DataFetchingEnvironment env) {
        UnitOfMeasure uom = q.getUnits();
        return env.<Long, UnitOfMeasure>getDataLoader(UnitOfMeasureBatchLoader.class.getName())
                .load(uom == null ? null : uom.getId());
    }

}
