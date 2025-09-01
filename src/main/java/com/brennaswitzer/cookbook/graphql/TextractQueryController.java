package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.payload.TextractJobInfo;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.storage.StorageService;
import com.brennaswitzer.cookbook.services.textract.TextractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class TextractQueryController {

    record TextractQuery() {}

    @Autowired
    private TextractService service;

    @Autowired
    private StorageService storageService;

    @QueryMapping
    TextractQuery textract() {
        return new TextractQuery();
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    List<TextractJobInfo> listJobs(TextractQuery textractQ,
                                   @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return service.getMyJobs(userPrincipal)
                .stream()
                .map(j -> TextractJobInfo.fromJobWithLines(j, storageService))
                .toList();
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    TextractJobInfo jobById(TextractQuery textractQ,
                            @Argument Long id,
                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        TextractJob job = service.getJob(userPrincipal, id);
        return TextractJobInfo.fromJobWithLines(job, storageService);
    }

}
