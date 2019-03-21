package com.tardisone.companyservice.controller.user;

import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/company")
public class UserController {

    @Autowired
    UserService userService;

    @Value("${application.frontEndAddress}")
    String frontEndAddress;

    @GetMapping(value = "user/verify/{activationToken}")
    public String finishUserVerification(@PathVariable String activationToken) {
        Boolean result = userService.finishUserVerification(activationToken);
        if (result) {
            return String.format("redirect:%saccount/verify/done", frontEndAddress);
        }
        return String.format("redirect:%serror", frontEndAddress);
    }
}
