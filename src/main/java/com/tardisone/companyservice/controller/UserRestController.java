package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRestController {
    @Autowired
    UserService userService;

    @PostMapping(value = "/user/register/email")
    public HttpEntity sendVerifyEmail(String email) {
        Boolean result = userService.sendVerifyEmail(email);

        if (result) {
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(value = "/user/verify/{activationToken}")
    public HttpEntity finishUserVerification(@PathVariable String activationToken) {

        return new ResponseEntity(HttpStatus.OK);
    }
}
