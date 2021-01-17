package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TextractService {

    @Autowired
    protected UserPrincipalAccess principalAccess;

    @Autowired
    protected TextractJobRepository jobRepository;

    public List<TextractJob> getQueue() {
        return jobRepository.findAllByOwnerOrderByCreatedAtDesc(principalAccess.getUser());
    }

    public TextractJob getJob(long id) {
        return jobRepository.getOne(id);
    }

}
