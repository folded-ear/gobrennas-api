package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.config.CalendarProperties;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.services.PlanCalendar;
import com.brennaswitzer.cookbook.util.ShareHelper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping({ "/shared/plan" })
@Slf4j
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
        Calendar calendar;
        try {
            calendar = planCalendar.getCalendar(id);
        } catch (EntityNotFoundException enfe) {
            log.warn(enfe.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Plan Not Found",
                    enfe);
        }
        response.addHeader(HttpHeaders.CONTENT_TYPE,
                           "txt".equals(ext) ? "text/plain" : "text/calendar");
        new CalendarOutputter(calendarProperties.isValidate())
                .output(calendar,
                        response.getWriter());
    }

}
