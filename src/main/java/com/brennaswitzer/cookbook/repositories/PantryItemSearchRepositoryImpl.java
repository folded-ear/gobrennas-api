package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import com.brennaswitzer.cookbook.util.EnglishUtils;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class PantryItemSearchRepositoryImpl implements PantryItemSearchRepository {

    @Autowired
    private EntityManager entityManager;

    @Override
    public SearchResponse<PantryItem> search(PantryItemSearchRequest request) {
        var stmt = new NamedParameterQuery(
                """
                        select item
                        from PantryItem item
                           left join item.synonyms syn
                           left join item.labels.label lbl
                        """);
        int i = 0;
        for (var word : EnglishUtils.canonicalize(request.getFilter())
                .split(" ")) {
            if (word.isBlank()) continue;
            stmt.append(i == 0 ? "where" : "or");
            String p = "p" + (i++);
            stmt.append(String.format("""
                                              (upper(item.name) like upper('%%' || :%1$s || '%%') escape '\\'
                                               or upper(syn) like upper('%%' || :%1$s || '%%') escape '\\'
                                               or upper(lbl.name) like upper('%%' || :%1$s || '%%') escape '\\')
                                              """, p), p, word);
        }
        stmt.append("order by ");
        for (var sort : request.getSort()) {
            stmt.append("item.")
                    .append(sort.getProperty())
                    .append(" ")
                    .append(sort.getDirection().toString())
                    .append(", ");
        }
        stmt.append("item.id");
        TypedQuery<PantryItem> query = entityManager.createQuery(stmt.getStatement(), PantryItem.class)
                .setMaxResults(request.getLimit() + 1);
        if (request.isOffset()) {
            query.setFirstResult(request.getOffset());
        }
        stmt.forEachParameter(query::setParameter);

        return SearchResponse.of(request, query.getResultList());
    }

}
