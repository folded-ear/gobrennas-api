package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Owned;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.security.CurrentUser;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class OwnedResolver {

    @SchemaMapping
    public User ownedBy(Owned owned,
                        @CurrentUser UserPrincipal userPrincipal) {
        if (mine(owned, userPrincipal)) return null;
        return owned.getOwner();
    }

    @SchemaMapping
    public boolean mine(Owned owned,
                        @CurrentUser UserPrincipal userPrincipal) {
        return userPrincipal != null
               && owned.isOwner(userPrincipal);
    }

    @SchemaMapping
    public boolean notMine(Owned owned,
                           @CurrentUser UserPrincipal userPrincipal) {
        return !mine(owned, userPrincipal);
    }

}
