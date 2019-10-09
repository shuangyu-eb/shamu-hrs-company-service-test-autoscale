package shamu.company.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.service.JobUserService;

@RestApiController
public class JobController extends BaseRestController {

  private final JobUserService jobUserService;

  @Autowired
  public JobController(final JobUserService jobUserService) {
    this.jobUserService = jobUserService;
  }

  @PatchMapping("users/{id}/jobs")
  @PreAuthorize("hasPermission(#id,'USER', 'EDIT_USER')")
  public HttpEntity updateJobInfo(@PathVariable @HashidsFormat final Long id,
      @RequestBody final JobUpdateDto jobUpdateDto) {
    jobUserService.updateJobInfo(id, jobUpdateDto, getCompanyId());
    return new ResponseEntity(HttpStatus.OK);
  }

}
