package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.domain.UserPreference;
import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.ClearUserPreference;
import com.brennaswitzer.cookbook.services.DeleteUserDevice;
import com.brennaswitzer.cookbook.services.RenameUserDevice;
import com.brennaswitzer.cookbook.services.SetUserPreference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class ProfileMutation {

    @Autowired
    private ClearUserPreference clearUserPreference;
    @Autowired
    private SetUserPreference setUserPreference;
    @Autowired
    private RenameUserDevice renameUserDevice;
    @Autowired
    private DeleteUserDevice deleteUserDevice;

    @SchemaMapping(typeName = "ProfileMutation")
    @PreAuthorize("hasRole('USER')")
    public UserPreference clearPreference(@Argument String name,
                                          @Argument String deviceKey,
                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return clearUserPreference.clear(userPrincipal,
                                         name,
                                         deviceKey);
    }

    @SchemaMapping(typeName = "ProfileMutation")
    @PreAuthorize("hasRole('USER')")
    public UserPreference setPreference(@Argument String name,
                                        @Argument String deviceKey,
                                        @Argument String value,
                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return setUserPreference.set(userPrincipal,
                                     name,
                                     deviceKey,
                                     value);
    }

    @SchemaMapping(typeName = "ProfileMutation")
    @PreAuthorize("hasRole('USER')")
    public UserDevice renameDevice(@Argument Long id,
                                   @Argument String name,
                                   @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return renameUserDevice.rename(userPrincipal, id, name);
    }

    @SchemaMapping(typeName = "ProfileMutation")
    @PreAuthorize("hasRole('USER')")
    public Deletion deleteDevice(@Argument Long id,
                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return deleteUserDevice.delete(userPrincipal, id);
    }

}
