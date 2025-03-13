package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.services.storage.StorageService;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TextractJobInfo {

    public static TextractJobInfo fromJob(TextractJob job, StorageService storageService) {
        TextractJobInfo info = new TextractJobInfo();
        info.setId(job.getId());
        info.setPhoto(FileInfo.fromS3File(job.getPhoto(), storageService));
        info.setReady(job.isReady());
        return info;
    }

    public static TextractJobInfo fromJobWithLines(TextractJob job, StorageService storageService) {
        TextractJobInfo info = fromJob(job, storageService);
        if (job.isReady()) {
            info.setLines(job.getLines());
        }
        return info;
    }

    private Long id;
    private FileInfo photo;
    private boolean ready;
    private Set<TextractJob.Line> lines;

}
