package com.example.jobportal.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jobportal.application.model.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

}
