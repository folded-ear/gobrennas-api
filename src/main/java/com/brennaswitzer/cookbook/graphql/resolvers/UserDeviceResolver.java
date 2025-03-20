package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.UserDevice;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class UserDeviceResolver extends BaseEntityResolver<UserDevice> {

    public OffsetDateTime lastEnsuredAt(UserDevice d) {
        return d.getLastEnsuredAt()
                .atOffset(ZoneOffset.UTC);
    }

}
