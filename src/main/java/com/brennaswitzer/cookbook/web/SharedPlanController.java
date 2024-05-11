package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.config.CalendarProperties;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.services.PlanCalendar;
import com.brennaswitzer.cookbook.util.ShareHelper;
import net.fortuna.ical4j.data.CalendarOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.Writer;

@RestController
@RequestMapping({ "/shared/plan" })
public class SharedPlanController {

    @Autowired
    private CalendarProperties calendarProperties;

    @Autowired
    private ShareHelper helper;

    @Autowired
    private PlanCalendar planCalendar;

    @GetMapping(
            value = "/{slug}/{secret}/{id}.ics",
            produces = "text/calendar")
    public void getPlanCalendar(
            @PathVariable("secret") String secret,
            @PathVariable("id") Long id,
            Writer out) throws IOException {
        if (!helper.isSecretValid(Plan.class, id, secret)) {
            throw new AuthorizationServiceException("Bad secret");
        }
        new CalendarOutputter(calendarProperties.isValidate())
                .output(planCalendar.getCalendar(id),
                        out);
    }

}
