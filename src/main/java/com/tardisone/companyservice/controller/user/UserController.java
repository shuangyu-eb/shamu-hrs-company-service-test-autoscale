package com.tardisone.companyservice.controller.user;

import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("user/findUserByEmail/{email}")
    public User findUserByEmail(@PathVariable String email){
        return userService.findUserByEmail(email);
    }
}
