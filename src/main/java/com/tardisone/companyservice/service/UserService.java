package com.tardisone.companyservice.service;


import com.tardisone.companyservice.entity.JobUser;
import com.tardisone.companyservice.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    Boolean sendVerifyEmail(String email);

    Boolean finishUserVerification(String activationToken);

  User findUserByEmail(String email);

  Optional<User> findByEmailIgnoreCase(String email);


  User findByResetPasswordToken(String resetPasswordToken);

  boolean isExistByEmail(String email);

  User save(User user);

  User findByVerificationToken(String verificationToken);

  User findById(Long userId);

  Boolean sendInvitationEmail(User user);

  Boolean isExistByResetPasswordToken(String resetPasswordToken);

  List<Map> getAllManager();



}
