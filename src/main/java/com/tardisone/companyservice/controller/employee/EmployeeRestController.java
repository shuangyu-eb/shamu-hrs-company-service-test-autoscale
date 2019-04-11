package com.tardisone.companyservice.controller.employee;

import com.tardisone.companyservice.config.annotations.RestApiController;
import com.tardisone.companyservice.dto.JobUserDTO;
import com.tardisone.companyservice.dto.NormalObjectDTO;
import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.pojo.EmployeeInfomationPojo;
import com.tardisone.companyservice.pojo.OfficePojo;
import com.tardisone.companyservice.repository.OfficeRepository;
import com.tardisone.companyservice.service.UserService;
import com.tardisone.companyservice.utils.Constants;
import com.tardisone.companyservice.utils.FieldCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tardisone.companyservice.utils.FieldCheckUtil.checkAllFields;

@RestApiController
public class EmployeeRestController {

    @Autowired
    UserService userService;

    @GetMapping("employees")
    public List<JobUserDTO> getAllEmployees() {
        return userService.findAllEmployees();
    }

    @GetMapping(value = {"getJobInformation"})
    @ResponseBody
    @Transactional
    public List<NormalObjectDTO>  getJobInformation(){
        return userService.getJobInformation();
    }

    @PostMapping(value = "handleUserInformation")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleUserInformation(HttpServletRequest request){
        EmployeeInfomationPojo employeePojo = new EmployeeInfomationPojo(request);

        boolean checked = false;
        try{
            User user = userService.addNewUser(employeePojo);
            userService.handlePersonalInformation(employeePojo, user);
            if("true".equals(employeePojo.getSetupOption())){
                userService.handelEmergencyContacts(employeePojo, user);
            }
            userService.handleContactInformation(employeePojo, user);
            userService.handleJobInformation(employeePojo, user);
            userService.saveUser(user);
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
        return userService.saveEmploymentType(employmentType);
    }

    @PostMapping(value = "saveDepartment")
    @ResponseBody
    public Boolean saveDepartment(String department){
        return userService.saveDepartment(department);
    }

    @PostMapping(value = "saveOfficeLocation")
    @ResponseBody
    public Boolean saveOfficeLocation(HttpServletRequest request) {
        OfficePojo officePojo = new OfficePojo();
        officePojo.setCity(request.getParameter(Constants.CITY));
        officePojo.setOfficeName(request.getParameter(Constants.OFFICE_NAME));
        officePojo.setState(request.getParameter(Constants.STATE));
        officePojo.setStreet1(request.getParameter(Constants.STREET1));
        officePojo.setStreet2(request.getParameter(Constants.STREET2));

        return userService.saveOfficeLocation(officePojo);
    }


}
