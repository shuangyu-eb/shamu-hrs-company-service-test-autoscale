package com.tardisone.companyservice.controller.employee;

import com.tardisone.companyservice.config.annotations.RestApiController;
import com.tardisone.companyservice.dto.JobUserDTO;
import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RestApiController
public class EmployeeRestController {

    @Autowired
    UserService userService;

    @GetMapping("employees")
    public List<JobUserDTO> getAllEmployees() {
        return userService.findAllEmployees();
    }
}
