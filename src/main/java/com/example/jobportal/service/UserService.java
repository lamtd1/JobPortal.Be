package com.example.jobportal.service;

import com.example.jobportal.model.User;
import com.example.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    final private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User createUser(User user) {
        return userRepository.save(user);
    }
}