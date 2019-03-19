package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.pojo.Email;

public interface SendEmailService {
    Boolean inviteEmployee(User employee, User currentUser, Email email);
}
