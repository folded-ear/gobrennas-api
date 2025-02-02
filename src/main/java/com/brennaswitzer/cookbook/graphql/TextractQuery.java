package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.payload.TextractJobInfo;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.StorageService;
import com.brennaswitzer.cookbook.services.textract.TextractService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TextractQuery {

    @Autowired
    private TextractService service;

    @Autowired
    private StorageService storageService;

    public List<TextractJobInfo> listJobs(DataFetchingEnvironment env) {
        UserPrincipal p = PrincipalUtil.from(env);
        return service.getMyJobs(p)
                .stream()
                .map(j -> TextractJobInfo.fromJobWithLines(j, storageService))
                .toList();
    }

    public TextractJobInfo jobById(Long id,
                                   DataFetchingEnvironment env) {
        UserPrincipal p = PrincipalUtil.from(env);
        TextractJob job = service.getJob(p, id);
        return TextractJobInfo.fromJobWithLines(job, storageService);
    }

}
