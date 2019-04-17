package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.JobUser;
import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.repository.JobUserRepository;
import com.tardisone.companyservice.repository.UserRepository;
import com.tardisone.companyservice.service.JobUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JobUserServiceImpl implements JobUserService {

    @Autowired
    JobUserRepository jobUserRepository;

    @Override
    public JobUser findJobUserByUser(User user) {
        return jobUserRepository.findJobUserByUser(user);
    }

    @Override
    public void updateJobUser(JobUser jobUser){
        jobUserRepository.save(jobUser);
    }



}
