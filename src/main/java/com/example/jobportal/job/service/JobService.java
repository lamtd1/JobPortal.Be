package com.example.jobportal.job.service;

import com.example.jobportal.job.repository.JobRepository;
import com.example.jobportal.job.model.Job;

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
