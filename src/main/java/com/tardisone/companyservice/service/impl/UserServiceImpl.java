package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.repository.UserRepository;
import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmailWork(email);
    }
}
