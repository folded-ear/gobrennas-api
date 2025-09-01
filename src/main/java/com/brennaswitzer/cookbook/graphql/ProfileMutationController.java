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
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class ProfileMutationController {

    record ProfileMutation() {}

    @Autowired
    private ClearUserPreference clearUserPreference;
    @Autowired
    private SetUserPreference setUserPreference;
    @Autowired
    private RenameUserDevice renameUserDevice;
    @Autowired
    private DeleteUserDevice deleteUserDevice;

    @MutationMapping
    ProfileMutation profile() {
        return new ProfileMutation();
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    UserPreference clearPreference(ProfileMutation profileMut,
                                   @Argument String name,
                                   @Argument String deviceKey,
                                   @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return clearUserPreference.clear(userPrincipal,
                                         name,
                                         deviceKey);
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    UserPreference setPreference(ProfileMutation profileMut,
                                 @Argument String name,
                                 @Argument String deviceKey,
                                 @Argument String value,
                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return setUserPreference.set(userPrincipal,
                                     name,
                                     deviceKey,
                                     value);
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    UserDevice renameDevice(ProfileMutation profileMut,
                            @Argument Long id,
                            @Argument String name,
                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return renameUserDevice.rename(userPrincipal, id, name);
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    Deletion deleteDevice(ProfileMutation profileMut,
                          @Argument Long id,
                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return deleteUserDevice.delete(userPrincipal, id);
    }

}
