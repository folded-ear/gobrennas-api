package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Override
    List<Recipe> findAll();

    List<Recipe> findByOwner(User owner);

    @Query("select distinct recipe\n" +
            "from Recipe recipe\n" +
            "   left join recipe.labels rl\n" +
            "   left join rl.label label\n" +
            "where recipe.owner = :owner and ((LOWER(recipe.name) LIKE %:term%)\n" +
            "or (LOWER(label.name) LIKE %:term%))\n" +
            "order by recipe.name")
    Iterable<Recipe> findAllByOwnerAndTermIgnoreCase(
            @Param("owner") User owner,
            @Param("term") String filter
    );

    @Query("select distinct recipe\n" +
            "from Recipe recipe\n" +
            "   left join recipe.labels rl\n" +
            "   left join rl.label label\n" +
            "where LOWER(recipe.name) LIKE %:term%\n" +
            "or LOWER(label.name) LIKE %:term%\n" +
            "order by recipe.name")
    Iterable<Recipe> findAllByTermIgnoreCase(
            @Param("term") String filter
    );

    Optional<Recipe> findOneByOwnerAndNameIgnoreCase(User owner, String name);

    @Override
    Optional<Recipe> findById(Long aLong);

    @Override
    void deleteById(Long aLong);
}
