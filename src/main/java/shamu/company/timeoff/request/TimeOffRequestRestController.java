package shamu.company.timeoff.request;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.request.entity.TimeOffRequestApprovalStatus;

@RestApiController
public class TimeOffRequestRestController extends BaseRestController {

  private final TimeOffRequestService timeOffRequestService;

  @Autowired
  public TimeOffRequestRestController(TimeOffRequestService timeOffRequestService) {
    this.timeOffRequestService = timeOffRequestService;
  }

  @GetMapping("time-off_requests/approver/status/no-action/count")
  public Integer getTimeOffRequest() {

    return timeOffRequestService.getCountByApproverAndStatusIsNoAction(this.getUser());
  }

  @GetMapping("time-off-requests/approver")
  public List<TimeOffRequestDto> getTimeOffRequests(
      @RequestParam TimeOffRequestApprovalStatus[] status) {

    return timeOffRequestService.getByApproverAndStatus(this.getUser(), status).stream()
        .map(TimeOffRequestDto::new).collect(Collectors.toList());
  }


  @PatchMapping("time-off-requests/{id}")
  public HttpEntity updateTimeOffRequest(@PathVariable @HashidsFormat Long id) {

    return null;
  }
}
