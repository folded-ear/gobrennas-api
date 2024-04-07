package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import com.brennaswitzer.cookbook.util.EnglishUtils;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class PantryItemSearchRepositoryImpl implements PantryItemSearchRepository {

    @Autowired
    private EntityManager entityManager;

    private record ItemAndUse(PantryItem item, Long useCount) {}

    @Override
    public SearchResponse<PantryItem> search(PantryItemSearchRequest request) {
        var sortedByUseCount = request.isSorted()
                               && request.getSort()
                                       .stream()
                                       .anyMatch(s -> "useCount".equals(s.getProperty())
                                                      || "useCounts".equals(s.getProperty()));
        var stmt = new NamedParameterQuery("select distinct item\n");
        if (sortedByUseCount) {
            stmt.append("""
                                      , (select count(distinct r.id)
                                         from Recipe r
                                             join r.ingredients ref
                                         where ref.ingredient = item
                                        ) as use_count
                        """);
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
