package com.tardisone.companyservice.controller.account;


import com.tardisone.companyservice.dto.UserDto;
import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RestController
public class AccountRestController {

  @Autowired
  UserService userService;


  @GetMapping("/account/password/reset")
  public HttpEntity resetPassword(HttpServletRequest request,  String emailWork) {
    String emailAddress = "";
//    if (userDto != null) {
//      emailAddress = userDto.getEmailWork();
//    }
    emailAddress=emailWork;
    Optional<User> userOptional = userService.findByEmailIgnoreCase(emailAddress);
    if (userOptional.isPresent()) {
      User user = userOptional.get();
//      if (User.Status.UNVERIFIED.equals(user.getStatus())) {
//        throw new ForbiddenException(
//            "Your account is still unverified. Please activate your account first.", false);
//      }
      //Generating token
      String resetPasswordToken = UUID.randomUUID().toString();
      Timestamp timestamp = new Timestamp(new Date().getTime());
      user.setResetPasswordSentAt(timestamp);
      user.setResetPasswordToken(resetPasswordToken);
      userService.save(user);
      Boolean msg = true;
      if (msg) {
        return new ResponseEntity(HttpStatus.NO_CONTENT);
      }
    }
    return null;
  }

  @GetMapping("/account/password/reset/key")
  public HttpEntity resetPassword(String resetPasswordTokend) {
    User user = userService.findByResetPasswordToken(resetPasswordTokend);
    //user.setPassword(password);
    user.setResetPasswordToken(null);
    userService.save(user);
    return new ResponseEntity(HttpStatus.NO_CONTENT);
  }
}
