package com.tardisone.companyservice.service;

import com.tardisone.companyservice.dto.JobUserDTO;
import com.tardisone.companyservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User findUserByEmail(String email);

    void sendVerifyEmail(String email);

    void finishUserVerification(String activationToken);

    List<JobUserDTO> findAllEmployees();

    Boolean existsByEmailWork(String email);

    User getUser(Long id);
}
