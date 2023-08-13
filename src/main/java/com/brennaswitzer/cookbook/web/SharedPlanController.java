package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.config.CalendarProperties;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.services.PlanCalendar;
import com.brennaswitzer.cookbook.util.ShareHelper;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;

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
    public String getPlanCalendar(
            @PathVariable("secret") String secret,
            @PathVariable("id") Long id) throws IOException {
        if (!helper.isSecretValid(Plan.class, id, secret)) {
            throw new AuthorizationServiceException("Bad secret");
        }
        return convertToString(planCalendar.getCalendar(id));
    }

    private String convertToString(Calendar cal) throws IOException {
        // this would be better packaged as a response handler
        StringWriter out = new StringWriter();
        new CalendarOutputter(calendarProperties.isValidate())
                .output(cal, out);
        return out.toString();
    }

}
