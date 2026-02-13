package com.example.jobportal.service;

import com.example.jobportal.model.Job;
import com.example.jobportal.repository.JobRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {
    final private JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job createJob(Job job) {
        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }
}
