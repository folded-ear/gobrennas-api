package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import com.brennaswitzer.cookbook.util.EnglishUtils;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class PantryItemSearchRepositoryImpl implements PantryItemSearchRepository {

    private static final String USE_COUNT_BY_ID =
            """
            (select count(distinct r.id)
             from Recipe r
                 join r.ingredients ref
             where ref.ingredient.id = %1$s
            ) +
            (select count(*)
             from PlanItem
             where ingredient.id = %1$s
            )
            """;

    @Autowired
    private EntityManager entityManager;

    private record IdAndCount(Long id, Long count) {}

    private record ItemAndUse(PantryItem item, Long useCount) {}

    @Override
    public Map<PantryItem, Long> countTotalUses(Collection<PantryItem> items) {
        Map<PantryItem, Long> result = new HashMap<>();
        Map<Long, PantryItem> toFetchById = new HashMap<>();
        for (var it : items) {
            if (result.containsKey(it)) continue;
            if (it.getUseCount() == null) {
                toFetchById.put(it.getId(), it);
            } else {
                result.put(it, it.getUseCount());
            }
        }
        if (toFetchById.isEmpty()) return result;
        NamedParameterQuery q = new NamedParameterQuery(
                """
                select id
                """)
                .append(String.format(
                        """
                             , %s as use_count
                        """, String.format(USE_COUNT_BY_ID, "item.id")))
                .append("""
                        from PantryItem item
                        where id in :ids
                        """, "ids", toFetchById.keySet());
        var query = entityManager.createQuery(q.getStatement(),
                                              IdAndCount.class);
        q.forEachParameter(query::setParameter);
        query.getResultList().forEach(r -> {
            PantryItem item = toFetchById.get(r.id());
            item.setUseCount(r.count());
            result.put(item, r.count());
        });
        return result;
    }

    @Override
    public SearchResponse<PantryItem> search(PantryItemSearchRequest request) {
        var sortedByUseCount = request.isSorted()
                               && request.getSort()
                                       .stream()
                                       .anyMatch(s -> "useCount".equals(s.getProperty())
                                                      || "useCounts".equals(s.getProperty()));
        var stmt = new NamedParameterQuery("select distinct item\n");
        if (sortedByUseCount) {
            stmt.append(String.format(
                    """
                         , %s as use_count
                    """, String.format(USE_COUNT_BY_ID, "item.id")));
        }
        stmt.append("""
                    from PantryItem item
                       left join item.synonyms syn
                       left join item.labels.label lbl
                    """);
        if (request.isFiltered()) {
            int i = 0;
            for (var word : EnglishUtils.canonicalize(request.getFilter())
                    .split(" ")) {
                if (word.isBlank()) continue;
                stmt.append(i == 0 ? "where" : "or");
                String p = "p" + (i++);
                stmt.append(String.format(
                        """
                        (upper(item.name) like upper('%%' || :%1$s || '%%') escape '\\'
                         or upper(syn) like upper('%%' || :%1$s || '%%') escape '\\'
                         or upper(lbl.name) like upper('%%' || :%1$s || '%%') escape '\\')
                        """, p), p, word);
            }
        }
        stmt.append("order by ");
        if (request.isSorted()) {
            for (var sort : request.getSort()) {
                switch (sort.getProperty()) {
                    case "firstUse" -> stmt.identifier("item.createdAt");
                    case "useCount", "useCounts" -> stmt.append("use_count");
                    default -> stmt.identifier("item." + sort.getProperty());
                }
                stmt.append(" ")
                        .append(sort.getDirection().toString())
                        .append(", ");
            }
        }
        stmt.append("item.id");
        List<PantryItem> pantryItems;
        if (sortedByUseCount) {
            pantryItems = query(request, stmt, ItemAndUse.class)
                    .stream()
                    .map(iau -> {
                        iau.item().setUseCount(iau.useCount());
                        return iau.item();
                    })
                    .toList();
        } else {
            pantryItems = query(request, stmt, PantryItem.class);
        }
        return SearchResponse.of(request, pantryItems);
    }

    private <T> List<T> query(SearchRequest request,
                              NamedParameterQuery stmt,
                              Class<T> resultClazz) {
        TypedQuery<T> query = entityManager.createQuery(stmt.getStatement(),
                                                        resultClazz)
                .setMaxResults(request.getLimit() + 1);
        if (request.isOffset()) {
            query.setFirstResult(request.getOffset());
        }
        stmt.forEachParameter(query::setParameter);

        return query.getResultList();
    }

}
