package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.payload.TextractJobInfo;
import com.brennaswitzer.cookbook.services.StorageService;
import com.brennaswitzer.cookbook.services.TextractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/textract")
@PreAuthorize("hasRole('USER')")
public class TextractController {

    @Autowired
    private TextractService service;

    @Autowired
    private StorageService storageService;

    @GetMapping
    public List<TextractJobInfo> getJobs() {
        return service.getAllJobs().stream()
                .map(j -> TextractJobInfo.fromJob(j, storageService))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TextractJobInfo getJob(@PathVariable("id") long id) {
        TextractJob job = service.getJob(id);
        return TextractJobInfo.fromJobWithLines(job, storageService);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TextractJobInfo createJob(@RequestParam MultipartFile photo) {
        TextractJob job = service.createJob(photo);
        return TextractJobInfo.fromJob(job, storageService);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable("id") long id) {
        service.deleteJob(id);
    }

}
