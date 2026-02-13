package com.example.jobportal.controller;

import com.example.jobportal.model.Application;
import com.example.jobportal.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    final private ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // Apply for a job - only Logged in user can use this
    @PostMapping("/apply/{jobId}")
    public ResponseEntity<Application> applyForJob(
            @PathVariable Long jobId,
            Principal principal) {
        String username = principal.getName();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(applicationService.applyForJob(username, jobId));
    }
}
