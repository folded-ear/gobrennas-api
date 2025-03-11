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
import org.springframework.stereotype.Component;

@Component
public class ProfileMutation {

    @Autowired
    private ClearUserPreference clearUserPreference;
    @Autowired
    private SetUserPreference setUserPreference;
    @Autowired
    private RenameUserDevice renameUserDevice;
    @Autowired
    private DeleteUserDevice deleteUserDevice;

    public UserPreference clearPreference(String name, String deviceKey, DataFetchingEnvironment env) {
        return clearUserPreference.clear(PrincipalUtil.from(env),
                                         name,
                                         deviceKey);
    }

    public UserPreference setPreference(String name, String deviceKey, String value, DataFetchingEnvironment env) {
        return setUserPreference.set(PrincipalUtil.from(env),
                                     name,
                                     deviceKey,
                                     value);
    }

    public UserDevice renameDevice(Long id, String name, DataFetchingEnvironment env) {
        return renameUserDevice.rename(PrincipalUtil.from(env), id, name);
    }

    public Deletion deleteDevice(Long id, DataFetchingEnvironment env) {
        return deleteUserDevice.delete(PrincipalUtil.from(env), id);
    }

}
