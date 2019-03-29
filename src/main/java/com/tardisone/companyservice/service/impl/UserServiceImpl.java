package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.dto.JobUserDTO;
import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.exception.EmailException;
import com.tardisone.companyservice.repository.JobUserRepository;
import com.tardisone.companyservice.repository.UserRepository;
import com.tardisone.companyservice.service.UserService;
import com.tardisone.companyservice.utils.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    ITemplateEngine templateEngine;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JobUserRepository jobUserRepository;

    @Value("${application.systemEmailAddress}")
    String systemEmailAddress;

    @Value("${application.frontEndAddress}")
    String frontEndAddress;

    @Autowired
    EmailUtil emailUtil;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmailWork(email);
    }

    @Override
    public Boolean sendVerifyEmail(String email) {
        User user = userRepository.findByEmailWork(email);
        if (user == null) {
            return false;
        }

        String accountVerifyToken = UUID.randomUUID().toString();
        String emailContent = getActivationEmail(accountVerifyToken);
        Boolean emailResult = emailUtil.send(systemEmailAddress, email, "Please activate your account!", emailContent);
        if (!emailResult) {
            throw new EmailException("Error when sending out verify email!");
        }

        user.setVerificationToken(accountVerifyToken);
        userRepository.save(user);
        return true;
    }

    @Override
    public Boolean finishUserVerification(String activationToken) {
        User user = userRepository.findByVerificationToken(activationToken);
        if (user == null || user.getVerifiedAt() != null) {
            return false;
        }
        user.setVerifiedAt(new Timestamp(new Date().getTime()));
        userRepository.save(user);
        return true;
    }

    @Override
    public List<JobUserDTO> findAllEmployees(Long id) {
        return jobUserRepository.findAllEmployees(id);
    }

    public String getActivationEmail(String accountVerifyToken) {
        Context context = new Context();
        context.setVariable("frontEndAddress", frontEndAddress);
        context.setVariable("accountVerifyAddress", String.format("account/verify/%s", accountVerifyToken));
        return templateEngine.process("account_verify_email.html", context);
    }
}
