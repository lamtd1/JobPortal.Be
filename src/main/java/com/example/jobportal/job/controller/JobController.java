package com.example.jobportal.job.controller;

import com.example.jobportal.job.model.Job;
import com.example.jobportal.job.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class JobController {

    final private JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // Create a new job
    @PostMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(jobService.createJob(job));
    }

    // Get all jobs
    @GetMapping("/jobs/all")
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(jobService.getAllJobs());
    }
}
