package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.payload.TextractJobInfo;
import com.brennaswitzer.cookbook.services.storage.StorageService;
import com.brennaswitzer.cookbook.services.textract.TextractService;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TextractMutation {

    @Autowired
    private TextractService service;

    @Autowired
    private StorageService storageService;

    public TextractJobInfo createPreUploadedJob(String filename,
                                                DataFetchingEnvironment env) {
        return TextractJobInfo.fromJobWithLines(
                service.createPreUploadedJob(
                        PrincipalUtil.from(env),
                        filename),
                storageService);
    }

    public Deletion deleteJob(Long id,
                              DataFetchingEnvironment env) {
        return Deletion.of(service.deleteJob(PrincipalUtil.from(env),
                                             id));
    }

}
