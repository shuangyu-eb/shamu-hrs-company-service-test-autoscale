package com.tardisone.companyservice.service.impl;

import java.sql.Timestamp;
import java.util.*;

import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.exception.EmailException;
import com.tardisone.companyservice.repository.*;
import com.tardisone.companyservice.service.UserService;
import com.tardisone.companyservice.utils.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ITemplateEngine templateEngine;

    @Autowired
    UserPersonalInformationRepository userPersonalInformationRepository;

    @Autowired
    private UserContactlInformationRepository userContactlInformationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobUserRepository jobUserReposiory;

    @Autowired
    private DepartmentRepository departmentRepository;


    @Value("${application.systemEmailAddress}")
    String systemEmailAddress;

    @Value("${application.frontEndAddress}")
    String frontEndAddress;

    @Autowired
    EmailUtil emailUtil;


    @Override
    public User findUserByEmail(String emailWork) {
        return userRepository.findByEmailWork(emailWork);
    }

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    @Override
    public User findByResetPasswordToken(String resetPasswordToken) {
        return userRepository.findByResetPasswordToken(resetPasswordToken)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public boolean isExistByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).isPresent();
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findByVerificationToken(String verificationToken) {
        return userRepository.findByVerificationToken(verificationToken);
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }


    @Override
    @Transactional
    public Boolean sendInvitationEmail(User user) {
        //String inviter ="";//authUserService.getUser().getFirstName() + ' ' + authUserService.getUser().getLastName();
        String verifyToken = UUID.randomUUID().toString();
        user.setInvitationEmailToken(verifyToken);
        user.setInvitedAt(new Timestamp(System.currentTimeMillis()));
        save(user);
        return true;
    }

    @Override
    public Boolean isExistByResetPasswordToken(String resetPasswordToken) {
        return userRepository.existsByResetPasswordToken(resetPasswordToken);
    }

    @Override
    public List<Map> getAllManager() {
        return userRepository.getAllManager();
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

    public String getActivationEmail(String accountVerifyToken) {
        Context context = new Context();
        context.setVariable("frontEndAddress", frontEndAddress);
        context.setVariable("accountVerifyAddress", String.format("account/verify/%s", accountVerifyToken));
        return templateEngine.process("account_verify_email.html", context);
    }
}
