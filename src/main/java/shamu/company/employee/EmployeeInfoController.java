package shamu.company.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.job.JobUser;
import shamu.company.job.JobUserDto;
import shamu.company.job.JobUserRepository;
import shamu.company.user.UserService;
import shamu.company.user.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestApiController
public class EmployeeInfoController {

    @Autowired
    JobUserRepository jobUserService;

    @Autowired
    UserService userService;

    @GetMapping("employee-info/{id}")
    public Map getEmployeeInfoByUserId(@PathVariable String id){
        Map result = new HashMap();
        User employee = userService.findEmployeeInfoByEmployeeNumber(id);
        JobUserDto jobUserDTO =userService.findEmployeeInfoByEmployeeId(id);
        result.put("employeeFirstName",jobUserDTO.getFirstName());
        result.put("employeeImageUrl",jobUserDTO.getImageUrl());
        result.put("employeeWorkPhone",jobUserDTO.getPhoneNumber());
        result.put("employeeWorkEmail",jobUserDTO.getEmail());
        result.put("employeeJobTitle",jobUserDTO.getJobTitle());
        JobUser managerJob = jobUserService.findJobUserByUserId(Long.valueOf(employee.getManagerUser().getEmployeeNumber()));
        result.putIfAbsent("managerName", employee.getManagerUser().getUserPersonalInformation().getFirstName());
        result.put("managerImageUrl",employee.getManagerUser().getImageUrl());
        result.put("managerJobTitle",managerJob.getJob().getTitle());

        List<JobUserDto> reports = userService.findDirectReportsByManagerId(Long.valueOf(id));
        result.put("directReporters",reports);

        return result;
    }
}
