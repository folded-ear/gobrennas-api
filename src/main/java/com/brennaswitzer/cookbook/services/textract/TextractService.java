package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.Upload;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import com.brennaswitzer.cookbook.services.StorageService;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
public class TextractService {

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private TextractJobRepository jobRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public TextractJob getJob(long id) {
        return jobRepository.getReferenceById(id);
    }

    public TextractJob createJob(MultipartFile photo) {
        User user = principalAccess.getUser();
        TextractJob job = new TextractJob();
        job.setOwner(user);

        String name = photo.getOriginalFilename();
        if (name == null) {
            name = "photo";
        } else {
            name = S3File.sanitizeFilename(name);
        }
        String objectKey;
        objectKey = storageService.store(
                Upload.of(photo),
                "textract/" + user.getId() + "/" + job.get_eqkey() + "/" + name);
        job.setPhoto(new S3File(
                objectKey,
                photo.getContentType(),
                photo.getSize()
        ));
        val savedJob = jobRepository.save(job);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new JobCreatedEvent(savedJob.getId()));
            }
        });

        return savedJob;
    }

    public List<TextractJob> getAllJobs() {
        User user = principalAccess.getUser();
        return jobRepository.findAllByOwnerOrderByCreatedAtDesc(user);
    }

    public void deleteJob(long id) {
        jobRepository.findById(id).map(j -> {
            storageService.remove(j.getPhoto().getObjectKey());
            jobRepository.delete(j);
            return j;
        });
    }

}
