package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.config.CalendarProperties;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.services.PlanCalendar;
import com.brennaswitzer.cookbook.util.ShareHelper;
import jakarta.servlet.http.HttpServletResponse;
import net.fortuna.ical4j.data.CalendarOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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
            value = "/{slug}/{secret}/{id}.{ext:ics|txt}")
    public void getPlanCalendar(
            @PathVariable("secret") String secret,
            @PathVariable("id") Long id,
            @PathVariable("ext") String ext,
            HttpServletResponse response) throws IOException {
        if (!helper.isSecretValid(Plan.class, id, secret)) {
            throw new AuthorizationServiceException("Bad secret");
        }
        response.setCharacterEncoding("UTF-8");
        response.addHeader(HttpHeaders.CONTENT_TYPE, switch (ext) {
            case "txt" -> "text/plain";
            default -> "text/calendar";
        });
        new CalendarOutputter(calendarProperties.isValidate())
                .output(planCalendar.getCalendar(id),
                        response.getWriter());
    }

}
