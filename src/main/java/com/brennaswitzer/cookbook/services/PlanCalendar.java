package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.PlanBucketRepository;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.FluentCalendar;
import net.fortuna.ical4j.model.FluentComponent;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Email;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;

@Service
@Transactional
public class PlanCalendar {

    private static final String PROD_ID = "-//Brenna's Food Software//NONSGML BFS API/plan Plan 1.0/EN";

    @Autowired
    private PlanRepository planRepo;

    @Autowired
    private PlanBucketRepository bucketRepo;

    @Autowired
    private AppProperties appProperties;

    private VEvent getEvent(PlanItem item) {
        FluentComponent event = new VEvent()
                .withProperty(getEventSummary(item))
                .withProperty(getEventStartDate(item))
                .withProperty(getEventUid(item))
                .withProperty(getEventSequence(item))
                .withProperty(getEventOrganizer(item))
                .withProperty(getEventTransparency(item))
                .withProperty(getEventDescription(item));
        if (item.isInTrashBin()) {
            event.withProperty(Method.CANCEL)
                    .withProperty(Status.VEVENT_CANCELLED);
        }
        return event.getFluentTarget();
    }

    @NotNull
    private Transp getEventTransparency(PlanItem item) {
        return Transp.TRANSPARENT;
    }

    private Description getEventDescription(PlanItem item) {
        Plan plan = item.getPlan();
        return new Description(String.format(
                "Cook: <a href=\"%splan/%s/recipe/%s\">%s</a>%n" +
                        "%n" +
                        "Plan: <a href=\"%1$splan/%2$s\">%s</a>",
                appProperties.getPublicUrl(),
                plan.getId(),
                item.getId(),
                item.getName(),
                plan.getName()));
    }

    private Summary getEventSummary(PlanItem item) {
        String name = item.getName();
        PlanBucket bucket = item.getBucket();
        if (bucket.isNamed()) {
            name += " - " + bucket.getName();
        }
        return new Summary(name);
    }

    @NotNull
    private DtStart getEventStartDate(PlanItem item) {
        java.util.Date dt = java.util.Date.from(
                item.getBucket()
                        .getDate()
                        .atStartOfDay(ZoneId.systemDefault())
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
                item.getModCount()
                        + item.getBucket().getModCount()));
    }

    private Organizer getEventOrganizer(PlanItem item) {
        User owner = item.getPlan().getOwner();
        return new Organizer()
                .withParameter(new Cn(owner.getName()))
                .withParameter(new Email(owner.getEmail()))
                .getFluentTarget();
    }

    /**
     * I convert the passed stream of PlanItems to an iCal calendar.
     */
    public Calendar getCalendar(long planId) {
        Plan plan = planRepo.getReferenceById(planId);
        FluentCalendar cal = new Calendar()
                .withProdId(PROD_ID)
                .withProperty(new Summary(plan.getName() + " - Brenna's Food Software"))
                .withProperty(Version.VERSION_2_0)
                .withProperty(CalScale.GREGORIAN)
                .withProperty(new RefreshInterval(
                        new ParameterList(),
                        Duration.of(4, ChronoUnit.HOURS)));
        bucketRepo.streamAllByPlanIdAndDateIsNotNull(planId)
                .sorted(Comparator.comparing(PlanBucket::getDate))
                .map(PlanBucket::getItems)
                .flatMap(Collection::stream)
                .map(this::getEvent)
                .forEach(cal::withComponent);
        return cal.getFluentTarget();
    }

}
