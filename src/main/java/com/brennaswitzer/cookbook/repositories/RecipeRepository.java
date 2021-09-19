package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends BaseEntityRepository<Recipe>, RecipeSearchRepository {

    @Override
    List<Recipe> findAll();

    List<Recipe> findByOwner(User owner);

    @Query("select distinct recipe\n" +
            "from Recipe recipe\n" +
            "   left join recipe.labels rl\n" +
            "   left join rl.label label\n" +
            "where recipe.owner = :owner\n" +
            "    and (lower(recipe.name) LIKE %:term%\n" +
            "        or lower(recipe.directions) LIKE %:term%\n" +
            "        or lower(label.name) LIKE %:term%\n" +
            "    )\n" +
            "order by recipe.name")
    Iterable<Recipe> findAllByOwnerAndTermContainingIgnoreCase(
            @Param("owner") User owner,
            @Param("term") String filter
    );

    @Query("select distinct recipe\n" +
            "from Recipe recipe\n" +
            "   left join recipe.labels rl\n" +
            "   left join rl.label label\n" +
            "where lower(recipe.name) LIKE %:term%\n" +
            "    or lower(recipe.directions) LIKE %:term%\n" +
            "    or lower(label.name) LIKE %:term%\n" +
            "order by recipe.name")
    Iterable<Recipe> findAllByTermContainingIgnoreCase(
            @Param("term") String filter
    );

    List<Recipe> findByOwnerAndNameIgnoreCaseOrderById(User owner, String name);

    List<Recipe> findAllByOwnerAndNameIgnoreCaseContainingOrderById(User owner, String name);

    @Override
    Optional<Recipe> findById(Long aLong);

    @Override
    void deleteById(Long aLong);
}
