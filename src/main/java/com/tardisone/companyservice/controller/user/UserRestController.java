package com.tardisone.companyservice.controller.user;

import com.tardisone.companyservice.pojo.Email;
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

    @PostMapping(value = "user/register/email")
    public HttpEntity sendVerifyEmail(@RequestBody Email email) {
        userService.sendVerifyEmail(email.getTo());
        return new ResponseEntity(HttpStatus.OK);
    }
}
