package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.config.CalendarProperties;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.PlanBucketRepository;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import jakarta.transaction.Transactional;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.FluentCalendar;
import net.fortuna.ical4j.model.FluentComponent;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Email;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RefreshInterval;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Service
@Transactional
public class PlanCalendar {

    /**
     * Used to artificially inflate sequence numbers when a code change
     * requires an update to <em>all</em> events.
     */
    private static final int SEQUENCE_OFFSET = 3;

    private static final String PROD_ID = "-//Brenna's Food Software//NONSGML BFS API/plan Plan 1.0/EN";

    @Autowired
    private PlanRepository planRepo;

    @Autowired
    private PlanBucketRepository bucketRepo;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private CalendarProperties calendarProperties;

    private record State(PlanItemStatus status,
                         boolean hidden) {}

    private class GetEvent implements BiFunction<PlanItem, Boolean, VEvent> {

        private static final DateTimeFormatter BUCKET_DATE_FORMAT
                = DateTimeFormatter.ofPattern("eee, MMM d");

        Map<PlanItem, State> itemToState = new HashMap<>();

        @Override
        public VEvent apply(PlanItem item,
                            Boolean includeBucketLink) {
            FluentComponent event = new VEvent()
                    .withProperty(getEventSummary(item))
                    .withProperty(getEventStartDate(item))
                    .withProperty(getEventUid(item))
                    .withProperty(getEventSequence(item))
                    .withProperty(getEventOrganizer(item))
                    .withProperty(getEventTransparency(item))
                    .withProperty(getEventDescription(item,
                                                      includeBucketLink));
            if (getState(item).hidden()) {
                event.withProperty(Method.CANCEL)
                        .withProperty(Status.VEVENT_CANCELLED);
            }
            return event.getFluentTarget();
        }

        private Transp getEventTransparency(PlanItem ignoredItem) {
            return Transp.TRANSPARENT;
        }

        private Description getEventDescription(PlanItem item,
                                                boolean includeBucketLink) {
            Plan plan = item.getPlan();
            List<String> lines = new ArrayList<>();
            if (!getState(item).hidden()) {
                lines.add(String.format(
                        "Cook: <a href=\"%splan/%s/recipe/%s\">%s</a>",
                        appProperties.getPublicUrl(),
                        plan.getId(),
                        item.getId(),
                        getDisplayName(item)));
            }
            if (includeBucketLink) {
                PlanBucket bucket = item.getBucket();
                lines.add(String.format(
                        "Cook: <a href=\"%splan/%s/bucket/%s\">%s</a>",
                        appProperties.getPublicUrl(),
                        plan.getId(),
                        bucket.getId(),
                        getBucketLabel(bucket)));
            }
            lines.add(String.format(
                    "Plan: <a href=\"%splan/%s\">%s</a>",
                    appProperties.getPublicUrl(),
                    plan.getId(),
                    plan.getName()));
            return new Description(String.join("\n\n", lines));
        }

        private String getDisplayName(PlanItem item) {
            return item.isRecognitionDisallowed()
                    ? item.getName().substring(1)
                    : item.getName();
        }

        private String getBucketLabel(PlanBucket bucket) {
            return bucket.isNamed()
                    ? bucket.getName()
                    : bucket.getDate().format(BUCKET_DATE_FORMAT);
        }

        private Summary getEventSummary(PlanItem item) {
            StringBuilder sb = new StringBuilder();
            switch (getState(item).status()) {
                case COMPLETED -> sb.append("✔ ");
                case DELETED -> sb.append("✘ ");
            }
            sb.append(getDisplayName(item));
            return new Summary(sb.toString());
        }

        private DtStart getEventStartDate(PlanItem item) {
            java.util.Date dt = java.util.Date.from(
                    item.getBucket()
                            .getDate()
                            .atStartOfDay(ZoneOffset.UTC)
                            .toInstant());
            return new DtStart(new Date(dt));
        }

        private Uid getEventUid(PlanItem item) {
            User owner = item.getPlan().getOwner();
            return new Uid(String.format(
                    "planitem%d@user%d.gobrennas.com",
                    item.getId(),
                    owner.getId()));
        }

        private Sequence getEventSequence(PlanItem item) {
            return new Sequence(String.valueOf(
                    SEQUENCE_OFFSET
                    + item.getModCount()
                    + item.getBucket().getModCount()));
        }

        private Organizer getEventOrganizer(PlanItem item) {
            User owner = item.getPlan().getOwner();
            return new Organizer()
                    .withParameter(new Cn(owner.getName()))
                    .withParameter(new Email(owner.getEmail()))
                    .getFluentTarget();
        }

        private State getState(PlanItem item) {
            // why not computeIfAbsent? ConcurrentModificationException!
            State s = itemToState.get(item);
            if (s == null) {
                s = computeState(item);
                itemToState.put(item, s);
            }
            return s;
        }

        private State computeState(PlanItem item) {
            PlanItemStatus status = item.getStatus();
            if (!status.isForDelete() && item.hasParent()) {
                PlanItemStatus ps = getState(item.getParent()).status;
                if (ps.isForDelete()) status = ps;
            }
            boolean hidden = item.hasParent()
                             && getState(item.getParent()).hidden();

            if (!hidden && item.isInTrashBin()) {
                if (status == PlanItemStatus.DELETED
                    && Duration.between(item.getCreatedAt(), item.getUpdatedAt())
                               .toHours() <= calendarProperties.getHoursDeletedWithinToCancel()) {
                    // deleted soon after creation
                    hidden = true;
                } else if (Duration.between(item.getUpdatedAt(), Instant.now())
                                   .toDays() > appProperties.getDaysInTrashBin() / 2) {
                    // been in the trash a while
                    hidden = true;
                }
            }

            return new State(status, hidden);
        }

    }

    public Calendar getCalendar(long planId) {
        Plan plan = planRepo.getReferenceById(planId);
        FluentCalendar cal = new Calendar()
                .withProdId(PROD_ID)
                .withProperty(new Summary(plan.getName() + " - Brenna's Food Software"))
                .withProperty(Version.VERSION_2_0)
                .withProperty(CalScale.GREGORIAN)
                .withProperty(Method.PUBLISH)
                .withProperty(getRefreshInterval());
        GetEvent getEvent = new GetEvent();
        bucketRepo.streamAllByPlanIdAndDateIsNotNull(planId)
                .sorted(Comparator.comparing(PlanBucket::getDate))
                .forEach(bucket -> {
                    Collection<PlanItem> items = bucket.getItems();
                    boolean includeBucketLink = (bucket.isNamed() || items.size() > 1)
                                                && items.stream()
                                                        .anyMatch(it -> !getEvent.getState(it).hidden());
                    for (PlanItem it : items) {
                        cal.withComponent(getEvent.apply(
                                it,
                                includeBucketLink));
                    }
                });
        return cal.getFluentTarget();
    }

    private RefreshInterval getRefreshInterval() {
        ParameterList params = new ParameterList();
        params.add(Value.DURATION);
        return new RefreshInterval(
                params,
                Duration.of(4, ChronoUnit.HOURS));
    }

}
