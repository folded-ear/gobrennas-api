package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("_probes")
public class StatusController {

    @Autowired
    private UserRepository userRepo;

    // grouping these together for now
    @GetMapping(
            value = {
                    // for startup
                    "/startup",
                    // for liveness
                    "/liveness"
            },
            produces = "text/plain")
    public Object healthy() {
        userRepo.findByEmail("nope");
        return "Happy Cooking!";
    }

}
