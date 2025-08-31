package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlannedRecipeHistoryRepository extends BaseEntityRepository<PlannedRecipeHistory> {

    /**
     * I return a list of recipe ids based on how likely the user is to want to
     * cook them, most likely first. The specific algorithm (and its parameters)
     * are subject to change.
     *
     * <p>Currently, recipes are ranked based on the user's (not other users')
     * ratings, decayed over time. If a recipe was cooked but not rated, a
     * 3-star rating is assumed. If a recipe was cooked two months ago, it will
     * contribute half the weight it would if it were cooked today. Four months
     * ago will contribute a quarter as much as today.
     */
    @Query(value = """
                   SELECT h.recipe_id
                     FROM planned_recipe_history h
                              JOIN ingredient i ON h.recipe_id = i.id
                    WHERE h.owner_id = :userId
                      AND h.status_id = 100 -- completed
                      AND i.section_of_id IS NULL -- not an owned section
                    GROUP BY h.recipe_id
                    ORDER BY SUM(EXP(LN(0.5) / 60 * (CURRENT_DATE - CAST(h.done_at AS DATE))) * COALESCE(h.rating, 3)) DESC
                           , 1
                    LIMIT :limit
                   OFFSET :offset
                   """,
            nativeQuery = true)
    List<Long> getRecipeSuggestions(Long userId, int offset, int limit);

}
