package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.UserPreference;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.services.ClearUserPreference;
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

}
