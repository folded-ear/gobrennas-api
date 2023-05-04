package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.Timer;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Timers are projected with initialDuration/duration, while the business layer
 * considers duration/extraTime.
 */
@SuppressWarnings("unused") // component-scanned for graphql-java
@Component
public class TimerResolver implements GraphQLResolver<Timer> {

    public List<AccessControlEntry> grants(Timer timer) {
        return AclHelpers.getGrants(timer);
    }

    public int initialDuration(Timer timer) {
        return timer.getDuration();
    }

    public int duration(Timer timer) {
        return timer.getDuration() + timer.getExtraTime();
    }

    /**
     * GraphQL Java speaks {@link OffsetDateTime}, not {@link Instant}, so
     * convert.
     */
    public OffsetDateTime endAt(Timer timer) {
        if (timer.isPaused()) return null;
        return timer.getEndAt()
                .atOffset(ZoneOffset.UTC);
    }

}
