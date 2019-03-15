package com.tardisone.companyservice.service.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.repository.UserRepository;
import com.tardisone.companyservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

  @Autowired
  UserRepository userRepository;

  @Override
  public Optional<User> findByEmailWork(String emailWork) {
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
    return userRepository.findByVerificationToken(verificationToken)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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

}

