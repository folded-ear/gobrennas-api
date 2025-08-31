package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.storage.ScratchSpace;
import com.brennaswitzer.cookbook.services.storage.StorageService;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@Transactional
public class TextractService {

    private static final String OBJECT_KEY_PREFIX = "textract/";

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private TextractJobRepository jobRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ScratchSpace scratchSpace;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public TextractJob getJob(UserPrincipal principal,
                              long id) {
        TextractJob job = jobRepository.getReferenceById(id);
        if (!job.getOwner().equals(principalAccess.getUser(principal))) {
            throw new EntityNotFoundException("Job #" + id + " not found");
        }
        return job;
    }

    @NotNull
    private TextractJob startJob(TextractJob job, S3File s3File) {
        job.setPhoto(s3File);
        val savedJob = jobRepository.save(job);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new JobCreatedEvent(savedJob.getId()));
            }
        });

        return savedJob;
    }

    public List<TextractJob> getMyJobs(UserPrincipal principal) {
        User user = principalAccess.getUser(principal);
        return jobRepository.findAllByOwnerOrderByCreatedAtDesc(user);
    }

    public TextractJob deleteJob(UserPrincipal principal,
                                 long id) {
        TextractJob j = getJob(principal, id);
        storageService.remove(j.getPhoto().getObjectKey());
        jobRepository.delete(j);
        return j;
    }

    public TextractJob createPreUploadedJob(UserPrincipal userPrincipal,
                                            String filename) {
        var user = principalAccess.getUser(userPrincipal);
        var file = scratchSpace.verifyUpload(user, filename);
        var job = new TextractJob();
        job.setOwner(user);
        var objectKey = buildObjectKey(job, file.filename());
        file.moveTo(objectKey);
        return startJob(job, new S3File(
                objectKey,
                file.contentType(),
                file.size()));
    }

    private String buildObjectKey(TextractJob job,
                                  String originalFilename) {
        return OBJECT_KEY_PREFIX
               + job.getOwner().getId()
               + "/"
               + job.get_eqkey()
               + "/"
               + S3File.sanitizeFilename(originalFilename);
    }

}
