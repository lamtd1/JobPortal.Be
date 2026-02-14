package com.example.jobportal.user.service;

import com.example.jobportal.user.model.User;
import com.example.jobportal.user.repository.UserRepository;

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