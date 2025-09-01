package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.domain.UserPreference;
import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.services.ClearUserPreference;
import com.brennaswitzer.cookbook.services.DeleteUserDevice;
import com.brennaswitzer.cookbook.services.RenameUserDevice;
import com.brennaswitzer.cookbook.services.SetUserPreference;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
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
    public UserPreference clearPreference(@Argument String name,
                                          @Argument String deviceKey,
                                          DataFetchingEnvironment env) {
        return clearUserPreference.clear(PrincipalUtil.from(env),
                                         name,
                                         deviceKey);
    }

    @SchemaMapping(typeName = "ProfileMutation")
    public UserPreference setPreference(@Argument String name,
                                        @Argument String deviceKey,
                                        @Argument String value,
                                        DataFetchingEnvironment env) {
        return setUserPreference.set(PrincipalUtil.from(env),
                                     name,
                                     deviceKey,
                                     value);
    }

    @SchemaMapping(typeName = "ProfileMutation")
    public UserDevice renameDevice(@Argument Long id,
                                   @Argument String name,
                                   DataFetchingEnvironment env) {
        return renameUserDevice.rename(PrincipalUtil.from(env), id, name);
    }

    @SchemaMapping(typeName = "ProfileMutation")
    public Deletion deleteDevice(@Argument Long id,
                                 DataFetchingEnvironment env) {
        return deleteUserDevice.delete(PrincipalUtil.from(env), id);
    }

}
