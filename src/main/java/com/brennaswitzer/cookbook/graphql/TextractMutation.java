package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.payload.TextractJobInfo;
import com.brennaswitzer.cookbook.services.storage.StorageService;
import com.brennaswitzer.cookbook.services.textract.TextractService;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class TextractMutation {

    @Autowired
    private TextractService service;

    @Autowired
    private StorageService storageService;

    @SchemaMapping(typeName = "TextractMutation")
    public TextractJobInfo createPreUploadedJob(@Argument String filename,
                                                DataFetchingEnvironment env) {
        return TextractJobInfo.fromJobWithLines(
                service.createPreUploadedJob(
                        PrincipalUtil.from(env),
                        filename),
                storageService);
    }

    @SchemaMapping(typeName = "TextractMutation")
    public Deletion deleteJob(@Argument Long id,
                              DataFetchingEnvironment env) {
        return Deletion.of(service.deleteJob(PrincipalUtil.from(env),
                                             id));
    }

}
