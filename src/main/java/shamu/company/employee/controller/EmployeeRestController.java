package shamu.company.employee.controller;

import shamu.company.employee.dto.GeneralObjectDTO;
import shamu.company.employee.pojo.EmployeeInfomationPojo;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.JobUserDto;
import shamu.company.user.UserService;
import shamu.company.utils.JobUserConstants;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import shamu.company.common.config.annotations.RestApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.user.entity.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestApiController
public class EmployeeRestController {

    @Autowired
    EmployeeService employeeService;

    @Autowired
    UserService userService;

    @GetMapping("employees")
    public List<JobUserDto> getAllEmployees() {
        return userService.findAllEmployees();
    }

    @GetMapping(value = {"getJobInformation"})
    @ResponseBody
    @Transactional
    public List<GeneralObjectDTO>  getJobInformation(){
        return employeeService.getJobInformation();
    }

    @PostMapping(value = "handleUserInformation")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleUserInformation(HttpServletRequest request){
        EmployeeInfomationPojo employeePojo = new EmployeeInfomationPojo(request);

        boolean checked = false;
        try{
            User user = employeeService.addNewUser(employeePojo);
            employeeService.handlePersonalInformation(employeePojo, user);
            employeeService.handelEmergencyContacts(employeePojo, user);
            employeeService.handleContactInformation(employeePojo, user);
            employeeService.handleJobInformation(employeePojo, user);
            employeeService.saveUser(user);
            checked = true;
        }catch (Exception e){
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }finally {
            return checked;
        }
    }

    @PostMapping(value = "saveEmploymentType")
    @ResponseBody
    public Boolean saveEmploymentType(String employmentType){
        return employeeService.saveEmploymentType(employmentType);
    }

    @PostMapping(value = "saveDepartment")
    @ResponseBody
    public Boolean saveDepartment(String department){
        return employeeService.saveDepartment(department);
    }

    @PostMapping(value = "saveOfficeLocation")
    @ResponseBody
    public Boolean saveOfficeLocation(HttpServletRequest request) {
        OfficePojo officePojo = new OfficePojo();
        officePojo.setCity(request.getParameter(JobUserConstants.CITY));
        officePojo.setOfficeName(request.getParameter(JobUserConstants.OFFICE_NAME));
        officePojo.setState(request.getParameter(JobUserConstants.STATE));
        officePojo.setStreet1(request.getParameter(JobUserConstants.STREET1));
        officePojo.setStreet2(request.getParameter(JobUserConstants.STREET2));

        return employeeService.saveOfficeLocation(officePojo);
    }


}
