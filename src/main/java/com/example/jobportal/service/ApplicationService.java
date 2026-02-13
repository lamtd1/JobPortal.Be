package com.example.jobportal.service;

import com.example.jobportal.model.Application;
import com.example.jobportal.model.Job;
import com.example.jobportal.model.User;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

        private final UserRepository userRepository;
        private final JobRepository jobRepository;
        private final ApplicationRepository applicationRepository;

        public ApplicationService(
                        UserRepository userRepository,
                        JobRepository jobRepository,
                        ApplicationRepository applicationRepository) {
                this.applicationRepository = applicationRepository;
                this.jobRepository = jobRepository;
                this.userRepository = userRepository;
        }

        public Application applyForJob(String username, Long jobId) {
                // fetch user obj using user_id else throw exception
                User user = userRepository.findByEmail(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // fetch job obj using job_id else throw exception
                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new RuntimeException("Job not found"));

                // Attach user & Job details to application obj
                Application application = new Application(user, job);
                // save it in DB using save()
                return applicationRepository.save(application);
        }
}
