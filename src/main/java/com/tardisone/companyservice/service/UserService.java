package com.tardisone.companyservice.service;

import com.tardisone.companyservice.dto.JobUserDTO;
import com.tardisone.companyservice.entity.User;

import java.util.List;

public interface UserService {
    User findUserByEmail(String email);

    Boolean sendVerifyEmail(String email);

    Boolean finishUserVerification(String activationToken);

    List<JobUserDTO> findAllEmployees();

    User findById(Long id);
}
