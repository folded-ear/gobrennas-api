package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.payload.FileInfo;
import com.brennaswitzer.cookbook.payload.TextractJobInfo;
import com.brennaswitzer.cookbook.services.StorageService;
import com.brennaswitzer.cookbook.services.TextractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @SubscribeMapping("/queue/textract")
    @SendToUser(destinations = "/queue/textract", broadcast = false)
    public List<TextractJobInfo> subscribeToQueue() {
        return service.getQueue()
                .stream()
                .map(j -> {
                    TextractJobInfo ji = new TextractJobInfo();
                    ji.setPhoto(FileInfo.fromS3File(j.getPhoto(), storageService));
                    ji.setReady(j.isReady());
                    return ji;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TextractJobInfo getJob(@PathVariable("id") long id) {
        TextractJob job = service.getJob(id);
        TextractJobInfo info = new TextractJobInfo();
        info.setPhoto(FileInfo.fromS3File(job.getPhoto(), storageService));
        info.setReady(job.isReady());
        if (job.isReady()) {
            info.setLines(job.getLines());
        }
        return info;
    }

}
