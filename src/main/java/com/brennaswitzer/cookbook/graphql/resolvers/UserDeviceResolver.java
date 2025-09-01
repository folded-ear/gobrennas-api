package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.UserDevice;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Controller
public class UserDeviceResolver extends BaseEntityResolver<UserDevice> {

    @SchemaMapping
    public OffsetDateTime lastEnsuredAt(UserDevice d) {
        return d.getLastEnsuredAt()
                .atOffset(ZoneOffset.UTC);
    }

}
