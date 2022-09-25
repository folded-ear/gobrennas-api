package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.TextractJobInfo;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TextractService {

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private TextractJobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private TextractProvider textractProvider;

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    public TextractJob getJob(long id) {
        return jobRepository.getOne(id);
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
        try {
            objectKey = storageService.store(
                    photo,
                    "textract/" + user.getId() + "/" + job.get_eqkey() + "/" + name);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to save photo", ioe);
        }
        job.setPhoto(new S3File(
                objectKey,
                photo.getContentType(),
                photo.getSize()
        ));
        job = jobRepository.save(job);

        broadcastQueueChange(user);

        Long userId = user.getId();
        Long jobId = job.getId();
        new Thread(() -> {
            try {
                textractProvider.processJob(jobId);
                broadcastQueueChange(userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return job;
    }

    public void broadcastQueueChange() {
        broadcastQueueChange(principalAccess.getUser());
    }

    private void broadcastQueueChange(Long userId) {
        broadcastQueueChange(userRepository.getById(userId));
    }

    private void broadcastQueueChange(User user) {
        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/textract",
                jobRepository.findAllByOwnerOrderByCreatedAtDesc(user)
                        .stream()
                        .map(j -> TextractJobInfo.fromJob(j, storageService))
                        .collect(Collectors.toList())
        );
    }

    public List<TextractJob> getAllJobs() {
        User user = principalAccess.getUser();
        return jobRepository.findAllByOwnerOrderByCreatedAtDesc(user);
    }

    public void deleteJob(long id) {
        jobRepository.findById(id).map(j -> {
            try {
                storageService.remove(j.getPhoto().getObjectKey());
            } catch (IOException ioe) {
                throw new RuntimeException("Failed to remove photo", ioe);
            }
            jobRepository.delete(j);
            broadcastQueueChange();
            return j;
        });
    }

}
