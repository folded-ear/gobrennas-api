package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Upload;
import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.payload.TextractJobInfo;
import com.brennaswitzer.cookbook.services.StorageService;
import com.brennaswitzer.cookbook.services.textract.TextractService;
import jakarta.servlet.http.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TextractMutation {

    @Autowired
    private TextractService service;

    @Autowired
    private StorageService storageService;

    public TextractJobInfo createJob(Part photo) {
        return TextractJobInfo.fromJobWithLines(service.createJob(Upload.of(photo)),
                                                storageService);
    }

    public Deletion deleteJob(Long id) {
        return Deletion.of(service.deleteJob(id));
    }

}
