package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import com.brennaswitzer.cookbook.util.EnglishUtils;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
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

    private static final String DUPLICATE_COUNT_BY_ID =
            """
            (select count(*)
             from PantryItemDuplicate
             where pantryItem.id = %1$s
               and not loose
            )
            """;

    @Autowired
    private EntityManager entityManager;

    private record IdAndCount(Long id, Long count) {}

    // Type shenanigans to allow Hibernate's reflective instantiation. An empty
    // string is used as semaphore for "no value" since the JDBC ResultSet won't
    // type a null-valued column, and JPA doesn't have casts like SQL.
    @SuppressWarnings("unused")
    private record ItemAndCounts(PantryItem item, Long useCount, Long dupeCount) {

        ItemAndCounts(PantryItem item, Long useCount, String ignoredDupeCount) {
            this(item, useCount, (Long) null);
        }

        ItemAndCounts(PantryItem item, String ignoredUseCount, Long dupeCount) {
            this(item, (Long) null, dupeCount);
        }

    }

    @VisibleForTesting
    Long countTotalUses(PantryItem item) {
        return countTotalUses(Collections.singleton(item))
                .get(item);
    }

    private <T extends Identified> Map<T, Long> countSomething(
            Collection<T> items,
            Function<T, Long> getter,
            BiConsumer<T, Long> setter,
            String countExpression) {
        Map<T, Long> result = new HashMap<>();
        Map<Long, T> toFetchById = new HashMap<>();
        for (var it : items) {
            if (result.containsKey(it)) continue;
            Long value = getter.apply(it);
            if (value == null) {
                toFetchById.put(it.getId(), it);
            } else {
                result.put(it, value);
            }
        }
        if (toFetchById.isEmpty()) return result;
        NamedParameterQuery q = new NamedParameterQuery(
                """
                select id
                """)
                .append(String.format(
                        """
                             , %s
                        """, String.format(countExpression, "item.id")))
                .append("""
                        from PantryItem item
                        where id in :ids
                        """, "ids", toFetchById.keySet());
        var query = entityManager.createQuery(q.getStatement(),
                                              IdAndCount.class);
        q.forEachParameter(query::setParameter);
        query.getResultList().forEach(r -> {
            T item = toFetchById.get(r.id());
            setter.accept(item, r.count());
            result.put(item, r.count());
        });
        return result;
    }

    @Override
    public Map<PantryItem, Long> countTotalUses(Collection<PantryItem> items) {
        return countSomething(items,
                              PantryItem::getUseCount,
                              PantryItem::setUseCount,
                              USE_COUNT_BY_ID);
    }

    @VisibleForTesting
    Long countDuplicates(PantryItem item) {
        return countDuplicates(Collections.singleton(item))
                .get(item);
    }

    @Override
    public Map<PantryItem, Long> countDuplicates(Collection<PantryItem> items) {
        return countSomething(items,
                              PantryItem::getDuplicateCount,
                              PantryItem::setDuplicateCount,
                              DUPLICATE_COUNT_BY_ID);
    }

    @Override
    public SearchResponse<PantryItem> search(PantryItemSearchRequest request) {
        boolean sortedByUseCount = false;
        boolean sortedByDuplicateCount = false;
        if (request.isSorted()) {
            for (var s : request.getSort()) {
                switch (s.getProperty()) {
                    case "useCount" -> sortedByUseCount = true;
                    case "duplicateCount" -> sortedByDuplicateCount = true;
                }
            }
        }
        var sortedByACount = sortedByUseCount || sortedByDuplicateCount;
        var stmt = new NamedParameterQuery("select distinct item\n");
        if (sortedByACount) {
            stmt.append(String.format(
                    """
                         , %s as use_count
                    """,
                    sortedByUseCount ? String.format(USE_COUNT_BY_ID, "item.id") : "''"));
            stmt.append(String.format(
                    """
                         , %s as dupe_count
                    """,
                    sortedByDuplicateCount ? String.format(DUPLICATE_COUNT_BY_ID, "item.id") : "''"));
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
                stmt.append(i == 0 ? "where (" : "or");
                String p = "p" + (i++);
                stmt.append(String.format(
                        """
                        (upper(item.name) like upper('%%' || :%1$s || '%%') escape '\\'
                         or upper(syn) like upper('%%' || :%1$s || '%%') escape '\\'
                         or upper(lbl.name) like upper('%%' || :%1$s || '%%') escape '\\')
                        """, p), p, word);
            }
            stmt.append(")\n");
        }
        if (request.isDuplicateOf()) {
            stmt.append(request.isFiltered() ? "and " : "where ")
                    .append("""
                            (item.id = :dupeOf
                             or exists (from PantryItemDuplicate
                                        where pantryItem.id = :dupeOf
                                          and duplicate.id = item.id
                                          and not loose
                                       )
                            )
                            """, "dupeOf", request.getDuplicateOf());
        }
        stmt.append("order by ");
        if (request.isSorted()) {
            for (var sort : request.getSort()) {
                switch (sort.getProperty()) {
                    case "firstUse" -> stmt.identifier("item.createdAt");
                    case "useCount" -> stmt.identifier("use_count");
                    case "duplicateCount" -> stmt.identifier("dupe_count");
                    default -> stmt.identifier("item." + sort.getProperty());
                }
                stmt.append(" ")
                        .append(sort.getDirection().toString())
                        .append(", ");
            }
        }
        stmt.append("item.id");
        List<PantryItem> pantryItems;
        if (sortedByACount) {
            pantryItems = query(request, stmt, ItemAndCounts.class)
                    .stream()
                    .map(iac -> {
                        PantryItem item = iac.item();
                        if (iac.useCount() != null) {
                            item.setUseCount(iac.useCount());
                        }
                        if (iac.dupeCount() != null) {
                            item.setDuplicateCount(iac.dupeCount());
                        }
                        return item;
                    })
                    .toList();
        } else {
            pantryItems = query(request, stmt, PantryItem.class);
        }
        if (request.isDuplicateOf()) {
            // If the target is in the page being returned, ensure it's first.
            Iterator<PantryItem> itr = pantryItems.iterator();
            for (int i = 0; itr.hasNext(); i++) {
                PantryItem curr = itr.next();
                if (request.getDuplicateOf().equals(curr.getId())) {
                    // Make a new connection, in case Hibernate returns
                    // something immutable.
                    pantryItems = new ArrayList<>(pantryItems);
                    pantryItems.add(0,
                                    pantryItems.remove(i));
                    break;
                }
            }
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
