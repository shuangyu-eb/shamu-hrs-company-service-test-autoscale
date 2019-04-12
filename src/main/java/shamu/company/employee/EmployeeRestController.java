package shamu.company.employee;

import shamu.company.common.config.annotations.RestApiController;
import shamu.company.job.JobUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.user.service.UserService;

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
