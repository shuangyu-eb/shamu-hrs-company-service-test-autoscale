package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.dto.JobUserDTO;
import com.tardisone.companyservice.entity.*;
import com.tardisone.companyservice.exception.ForbiddenException;
import com.tardisone.companyservice.repository.JobUserRepository;
import com.tardisone.companyservice.repository.UserAddressRepository;
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
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    ITemplateEngine templateEngine;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JobUserRepository jobUserRepository;

    @Autowired
    UserAddressRepository userAddressRepository;

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
    public void sendVerifyEmail(String email) {
        User user = userRepository.findByEmailWork(email);
        if (user == null) {
            throw new ForbiddenException("User account does not exist!");
        }

        String accountVerifyToken = UUID.randomUUID().toString();
        String emailContent = getActivationEmail(accountVerifyToken);
        emailUtil.send(systemEmailAddress, email, "Please activate your account!", emailContent);
        user.setVerificationToken(accountVerifyToken);
        userRepository.save(user);
    }

    @Override
    public void finishUserVerification(String activationToken) {
        User user = userRepository.findByVerificationToken(activationToken);
        if (user == null || user.getVerifiedAt() != null) {
            throw new ForbiddenException("User account does not exist or already activated!");
        }
        user.setVerifiedAt(new Timestamp(new Date().getTime()));
        userRepository.save(user);
    }

    @Override
    public List<JobUserDTO> findAllEmployees() {
        List<User> employees = userRepository.findAllEmployees();
        List<UserAddress> userAddresses = userAddressRepository.findAllByUserIn(employees);
        List<JobUser> jobUserList = jobUserRepository.findAllByUserIn(employees);

        return getJobUserDTOList(employees, userAddresses, jobUserList);
    }

    @Override
    public Boolean existsByEmailWork(String email) {
        return userRepository.existsByEmailWork(email);
    }

    @Override
    public User getUser(Long id) {
        return userRepository.getOne(id);
    }


    private List<JobUserDTO> getJobUserDTOList(List<User> employees, List<UserAddress> userAddresses, List<JobUser> jobUsers) {
        return employees.stream().map((employee) -> {
            JobUserDTO jobUserDTO = new JobUserDTO();
            jobUserDTO.setEmail(employee.getEmailWork());
            jobUserDTO.setImageUrl(employee.getImageUrl());
            jobUserDTO.setId(employee.getId());

            UserPersonalInformation userPersonalInformation = employee.getUserPersonalInformation();
            if (userPersonalInformation != null) {
                jobUserDTO.setFirstName(userPersonalInformation.getFirstName());
                jobUserDTO.setLastName(userPersonalInformation.getLastName());
            }

            userAddresses.forEach((userAddress -> {
                User userWithAddress = userAddress.getUser();
                if (userWithAddress != null
                        && userWithAddress.getId().equals(employee.getId())
                        && userAddress.getCity() != null) {
                    jobUserDTO.setCityName(userAddress.getCity());
                }
            }));

            jobUsers.forEach((jobUser -> {
                User userWithJob = jobUser.getUser();
                if (userWithJob != null
                        && userWithJob.getId().equals(employee.getId())
                        && jobUser.getJob() != null) {
                    jobUserDTO.setJobTitle(jobUser.getJob().getTitle());
                }
            }));
            return jobUserDTO;
        }).collect(Collectors.toList());
    }

    public String getActivationEmail(String accountVerifyToken) {
        Context context = new Context();
        context.setVariable("frontEndAddress", frontEndAddress);
        context.setVariable("accountVerifyAddress", String.format("account/verify/%s", accountVerifyToken));
        return templateEngine.process("account_verify_email.html", context);
    }
}
