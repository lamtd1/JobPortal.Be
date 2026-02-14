package com.example.jobportal.job.repository;

import com.example.jobportal.job.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
}
