package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import com.brennaswitzer.cookbook.services.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * I delete a Textract job, along with its photo.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteTextractJob {

    private final TextractJobRepository jobRepository;

    private final StorageService storageService;

    /**
     * I delete the job, if it exists, and return it.
     */
    public Optional<TextractJob> delete(long id) {
        Optional<TextractJob> claimed = jobRepository.claim(id);
        claimed.ifPresent(this::delete);
        return claimed;
    }

    /**
     * I delete the job, if it exists and was last updated before the
     * cutoff, and return it.
     */
    public Optional<TextractJob> deleteIfStale(long id, Instant cutoff) {
        Optional<TextractJob> claimed = jobRepository.claimIfStale(id, cutoff);
        claimed.ifPresent(this::delete);
        return claimed;
    }

    private void delete(TextractJob job) {
        storageService.remove(job.getPhoto().getObjectKey());
        jobRepository.delete(job);
    }

}
