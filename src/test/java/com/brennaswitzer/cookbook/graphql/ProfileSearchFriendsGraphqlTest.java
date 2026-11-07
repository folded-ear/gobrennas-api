package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.execution.GraphQlSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithAliceBobEve
class ProfileSearchFriendsGraphqlTest {

    private static final String SEARCH_FRIENDS = """
            query ($after: Cursor) {
              profile {
                searchFriends(search: { query: "gobrennas", first: 1, after: $after }) {
                  edges { node { id name email imageUrl } }
                  pageInfo { hasNextPage endCursor }
                }
              }
            }""";

    @Autowired
    private GraphQlSource source;

    @Autowired
    private UserRepository userRepository;

    @Test
    void pagesThroughFriends() {
        Map<?, ?> page = searchFriends(Map.of());
        assertEquals(List.of(node(friend("bob@gobrennas.com"))), nodes(page));
        assertEquals(true, pageInfo(page).get("hasNextPage"));

        page = searchFriends(Map.of("after", pageInfo(page).get("endCursor")));
        assertEquals(List.of(node(friend("eve@gobrennas.com"))), nodes(page));
        assertEquals(false, pageInfo(page).get("hasNextPage"));
    }

    private User friend(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow();
    }

    private Map<String, Object> node(User user) {
        var node = new HashMap<String, Object>();
        node.put("id", user.getId().toString());
        node.put("name", user.getName());
        node.put("email", user.getEmail());
        node.put("imageUrl", user.getImageUrl());
        return Map.of("node", node);
    }

    private Map<?, ?> searchFriends(Map<String, Object> variables) {
        ExecutionResult result = source.graphQl()
                .execute(ExecutionInput.newExecutionInput()
                                 .query(SEARCH_FRIENDS)
                                 .variables(variables)
                                 .build());
        assertEquals(List.of(), result.getErrors());
        Map<?, ?> data = result.getData();
        return (Map<?, ?>) ((Map<?, ?>) data.get("profile")).get("searchFriends");
    }

    private List<?> nodes(Map<?, ?> page) {
        return (List<?>) page.get("edges");
    }

    private Map<?, ?> pageInfo(Map<?, ?> page) {
        return (Map<?, ?>) page.get("pageInfo");
    }

}
