package com.tardisone.companyservice.controller.employee;

import com.tardisone.companyservice.dto.JobUserDTO;
import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/company")
public class EmployeeRestController {

    @Autowired
    UserService userService;

    @GetMapping("users/{id}/employees")
    public List<JobUserDTO> getAllEmployees(@PathVariable Long id) {
        return userService.findAllEmployees(id);
    }
}
