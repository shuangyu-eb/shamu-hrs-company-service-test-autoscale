package com.tardisone.companyservice.service;


import com.tardisone.companyservice.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface UserService {

  Optional<User> findByEmailWork(String email);

  Optional<User> findByEmailIgnoreCase(String email);


  User findByResetPasswordToken(String resetPasswordToken);

  boolean isExistByEmail(String email);

  User save(User user);

  User findByVerificationToken(String verificationToken);

  User findById(Long userId);

  Boolean sendInvitationEmail(User user);

  Boolean isExistByResetPasswordToken(String resetPasswordToken);
}
