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
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.SelectFieldSizeDto;
import shamu.company.job.dto.JobSelectOptionUpdateDto;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.service.JobUserService;

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
    jobUserService.updateJobInfo(id, jobUpdateDto, findCompanyId());
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

  @GetMapping("departments/{id}/jobs")
  @PreAuthorize("hasPermission(#id,'DEPARTMENT','VIEW_JOB')")
  public List<SelectFieldSizeDto> findJobsByDepartment(@PathVariable final String id) {
    return jobUserService.findJobsByDepartmentId(id);
  }
}
