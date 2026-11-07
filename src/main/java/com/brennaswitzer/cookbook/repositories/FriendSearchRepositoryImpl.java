package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Friend;
import com.brennaswitzer.cookbook.repositories.impl.FriendSearchRequest;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.query.EscapeCharacter;

public class FriendSearchRepositoryImpl implements FriendSearchRepository {

    @Autowired
    private EntityManager entityManager;

    @Override
    public SearchResponse<Friend> search(FriendSearchRequest request) {
        var stmt = new NamedParameterQuery(
                """
                select f
                from Friend f
                where f.id <> :me
                """,
                "me",
                request.getExcludedUserId());
        int i = 0;
        for (var term : request.getTerms()) {
            String p = "t" + (i++);
            stmt.append(String.format(
                    """
                      and (upper(f.name) like upper('%%' || :%1$s || '%%') escape '\\'
                        or upper(f.email) like upper('%%' || :%1$s || '%%') escape '\\')
                    """, p), p, EscapeCharacter.DEFAULT.escape(term));
        }
        stmt.append("order by upper(coalesce(f.name, f.email)), f.id");
        TypedQuery<Friend> query = entityManager.createQuery(stmt.getStatement(),
                                                             Friend.class)
                .setMaxResults(request.getLimit() + 1);
        if (request.isOffset()) {
            query.setFirstResult(request.getOffset());
        }
        stmt.forEachParameter(query::setParameter);
        return SearchResponse.of(request, query.getResultList());
    }

}
