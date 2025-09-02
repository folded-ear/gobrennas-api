package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.payload.TextractJobInfo;
import com.brennaswitzer.cookbook.security.CurrentUser;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.storage.StorageService;
import com.brennaswitzer.cookbook.services.textract.TextractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class TextractMutationController {

    record TextractMutation() {}

    @Autowired
    private TextractService service;

    @Autowired
    private StorageService storageService;

    @MutationMapping
    TextractMutation textract() {
        return new TextractMutation();
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    TextractJobInfo createPreUploadedJob(TextractMutation textractMut,
                                         @Argument String filename,
                                         @CurrentUser UserPrincipal userPrincipal) {
        return TextractJobInfo.fromJobWithLines(
                service.createPreUploadedJob(
                        userPrincipal,
                        filename),
                storageService);
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    Deletion deleteJob(TextractMutation textractMut,
                       @Argument Long id,
                       @CurrentUser UserPrincipal userPrincipal) {
        return Deletion.of(service.deleteJob(userPrincipal,
                                             id));
    }

}
