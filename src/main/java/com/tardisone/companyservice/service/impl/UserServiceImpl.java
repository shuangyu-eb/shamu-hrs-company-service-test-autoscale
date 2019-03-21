package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.exception.EmailException;
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
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    ITemplateEngine templateEngine;

    @Autowired
    UserRepository userRepository;

    @Value("${application.serverAddress}")
    String serverAddress;

    @Value("${application.systemEmailAddress}")
    String systemEmailAddress;

    @Value("${application.gatewayAddress}")
    String gatewayAddress;

    @Autowired
    EmailUtil emailUtil;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmailWork(email);
    }

    @Override
    public Boolean sendVerifyEmail(String email) {

        String accountVerifyToken = UUID.randomUUID().toString();
        String emailContent = getActivationEmail(accountVerifyToken);
        Boolean emailResult = emailUtil.send(systemEmailAddress, email, "Please activate your account!", emailContent);
        User user = userRepository.findByEmailWork(email);

        if (emailResult && user != null) {
            user.setVerificationToken(accountVerifyToken);
            userRepository.save(user);
            return true;
        }
        throw new EmailException("Error when sending out verify email!");
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

    public String getActivationEmail(String accountVerifyToken) {
        Context context = new Context();
        context.setVariable("serverAddress", serverAddress);
        context.setVariable("gatewayAddress", gatewayAddress);
        context.setVariable("accountVerifyAddress", String.format("company/user/verify/%s", accountVerifyToken));
        return templateEngine.process("account_verify_email.html", context);
    }
}
