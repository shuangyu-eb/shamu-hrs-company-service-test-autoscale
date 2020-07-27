package shamu.company.job;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.SelectFieldSizeDto;
import shamu.company.job.dto.JobSelectOptionUpdateDto;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.dto.JobUserHireDateCheckDto;
import shamu.company.job.service.JobUserService;
import shamu.company.user.dto.UserOfficeAndHomeAddressDto;

@RestApiController
public class JobController extends BaseRestController {

  private final JobUserService jobUserService;

  @Autowired
  public JobController(final JobUserService jobUserService) {
    this.jobUserService = jobUserService;
  }

  @GetMapping("users/{id}/job")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_JOB')")
  public BasicJobInformationDto findJobMessage(@PathVariable final String id) {
    return jobUserService.findJobMessage(id, findAuthUser().getId());
  }

  @PatchMapping("users/{id}/jobs")
  @PreAuthorize(
      "hasPermission(#id,'USER', 'EDIT_USER')"
          + "and hasPermission(#jobUpdateDto, 'USER_JOB', 'EDIT_USER')")
  public HttpEntity updateJobInfo(
      @PathVariable final String id, @RequestBody final JobUpdateDto jobUpdateDto) {
    jobUserService.updateJobInfo(id, jobUpdateDto);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("jobs/select/option/update")
  @PreAuthorize(
      "hasPermission("
          + "#jobSelectOptionUpdateDto.id, #jobSelectOptionUpdateDto.updateField, 'EDIT_USER')")
  public HttpEntity updateJobSelectOption(
      @RequestBody final JobSelectOptionUpdateDto jobSelectOptionUpdateDto) {
    jobUserService.updateJobSelectOption(jobSelectOptionUpdateDto);
    return new ResponseEntity(HttpStatus.OK);
  }

  @DeleteMapping("jobs/select/option/delete")
  @PreAuthorize(
      "hasPermission("
          + "#jobSelectOptionUpdateDto.id, #jobSelectOptionUpdateDto.updateField, 'EDIT_USER')")
  public HttpEntity deleteJobSelectOption(
      @RequestBody final JobSelectOptionUpdateDto jobSelectOptionUpdateDto) {
    jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("jobs")
  public List<SelectFieldSizeDto> findJobsByDepartment() {
    return jobUserService.findJobs();
  }

  @GetMapping("{userId}/check-job-info-complete")
  public boolean checkJobInfoComplete(@PathVariable final String userId) {
    return jobUserService.checkJobInfoComplete(userId);
  }

  @GetMapping("job/{userId}/hireDate")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')")
  public JobUserHireDateCheckDto checkUserHireDateDeletable(@PathVariable final String userId) {

    return jobUserService.checkUserHireDateDeletable(userId);
  }

  @PostMapping("job/homeAndOfficeAddresses")
  public List<UserOfficeAndHomeAddressDto> homeAndOfficeAddresses(
      @RequestBody final List<String> userIds) {
    return jobUserService.findHomeAndOfficeAddressByUsers(userIds);
  }
}
