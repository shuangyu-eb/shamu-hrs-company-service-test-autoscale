package com.tardisone.companyservice.controller.user;

import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
public class UserRestController {
    @Autowired
    UserService userService;

    @PostMapping(value = "user/sign-up/email")
    public HttpEntity sendVerifyEmail(@RequestBody String email) {
        Boolean emailResult = userService.sendVerifyEmail(email);
        if (emailResult) {
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "user/verify")
    public HttpEntity finishUserVerification(@RequestBody String token) {
        Boolean result = userService.finishUserVerification(token);
        if (result) {
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
}
