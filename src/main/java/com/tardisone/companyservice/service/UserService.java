package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.User;

public interface UserService {
    User findUserByEmail(String email);

    Boolean sendVerifyEmail(String email);
}
