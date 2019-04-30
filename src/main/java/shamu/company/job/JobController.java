package shamu.company.job;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.entity.Department;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.pojo.JobInformationPojo;
import shamu.company.job.pojo.OfficeAddressPojo;
import shamu.company.job.service.JobService;
import shamu.company.job.service.JobUserService;
import shamu.company.user.entity.User;

@RestApiController
public class JobController extends BaseRestController {

  @Autowired
  JobUserService jobUserService;

  @Autowired
  JobService jobService;

  @GetMapping(value = {"info/users/{userId}/jobs"})
  public JobInformationPojo getJobInfo(@PathVariable @HashidsFormat Long userId) {
    return jobUserService.getJobInfoByUserId(userId);
  }

  @GetMapping(value = {"info/office-addresses"})
  public List getOfficeAddresses() {
    return jobUserService.getOfficeAddresses(this.getUser());
  }

  @GetMapping(value = {"info/employment-types"})
  public List getEmploymentTypes() {
    return jobUserService.getEmploymentTypes(this.getUser());
  }

  @GetMapping(value = {"info/departments"})
  public List getDepartments() {
    return jobUserService.getDepartments(this.getUser());
  }

  @GetMapping(value = {"info/compensation-frequences"})
  public List getCompensationFrequences() {
    return jobUserService.getCompensationFrequences(this.getUser());
  }

  @GetMapping(value = {"info/state-provinces"})
  public List getStateProvinces() {
    return jobUserService.getStateProvinces();
  }

  @GetMapping(value = {"info/managers"})
  public List getManagers() {
    return jobUserService.getManagers(this.getUser());
  }

  @GetMapping(value = {"info/users/{userId}/jobusers"})
  public JobInformationPojo getJobInfoModal(@PathVariable @HashidsFormat Long userId) {
    return jobUserService.getJobInfoModal(userId);
  }

  @PatchMapping(value = {"info/users/jobs"})
  public HttpEntity updateJobInfo(@RequestBody JobInformationPojo jobInfoEditPojo) {
    jobService.updateJobInfo(jobInfoEditPojo, this.getUser());
    return new ResponseEntity(HttpStatus.OK);
  }

  @PostMapping(value = {"info/employee-types"})
  public HttpEntity createEmploymentType(@RequestBody EmploymentType type) {
    User user = this.getUser();
    jobService.saveEmploymentType(type, user.getCompany());
    return new ResponseEntity(HttpStatus.OK);
  }

  @PostMapping(value = {"info/deparments"})
  public HttpEntity createDepartment(@RequestBody Department department) {
    jobService.saveDepartment(department, this.getUser());
    return new ResponseEntity(HttpStatus.OK);
  }

  @PostMapping(value = {"info/offices/addresses"})
  public HttpEntity createOfficeAddress(@RequestBody OfficeAddressPojo addressPojo) {
    jobService.saveOfficeAddress(addressPojo);
    return new ResponseEntity(HttpStatus.OK);
  }
}
