package shamu.company.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.job.JobUser;
import shamu.company.job.JobUserDto;
import shamu.company.job.JobUserRepository;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestApiController
public class EmployeeInfoController {

    @Autowired
    JobUserRepository jobUserService;

    @Autowired
    UserService userService;

    @GetMapping("/employees/{id}/info")
    public Map getEmployeeInfoByUserId(@PathVariable Long id){
        Map result = new HashMap();
        JobUserDto jobUserDTO =userService.findEmployeeInfoByEmployeeId(id);
        result.put("employeeFirstName",jobUserDTO.getFirstName());
        result.put("employeeImageUrl",jobUserDTO.getImageUrl());
        result.put("employeeWorkPhone",jobUserDTO.getPhoneNumber());
        result.put("employeeWorkEmail",jobUserDTO.getEmail());
        result.put("employeeJobTitle",jobUserDTO.getJobTitle());

        User employee = userService.findEmployeeInfoByUserId(id);
        JobUserDto managerjobUserDTO =userService.findEmployeeInfoByEmployeeId(employee.getManagerUser().getId());
        result.put("managerName",managerjobUserDTO.getFirstName());
        result.put("managerImageUrl",managerjobUserDTO.getImageUrl());
        result.put("managerJobTitle",managerjobUserDTO.getJobTitle());

        List<JobUserDto> reports = userService.findDirectReportsByManagerId(id);
        result.put("directReporters",reports);

        return result;
    }
}
