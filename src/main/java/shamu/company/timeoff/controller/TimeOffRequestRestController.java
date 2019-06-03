package shamu.company.timeoff.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.pojo.TimeOffRequestPojo;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.timeoff.service.TimeOffRequestDateService;
import shamu.company.timeoff.service.TimeOffRequestService;

@RestApiController
public class TimeOffRequestRestController extends BaseRestController {

  private final TimeOffRequestService timeOffRequestService;

  private final TimeOffRequestDateService timeOffRequestDateService;

  private final TimeOffPolicyService timeOffPolicyService;

  @Autowired
  public TimeOffRequestRestController(TimeOffRequestService timeOffRequestService,
      TimeOffRequestDateService timeOffRequestDateService,
      TimeOffPolicyService timeOffPolicyService) {
    this.timeOffRequestService = timeOffRequestService;
    this.timeOffRequestDateService = timeOffRequestDateService;
    this.timeOffPolicyService = timeOffPolicyService;
  }

  @PostMapping("time-off-requests")
  public void createTimeOffRequest(@RequestBody TimeOffRequestPojo requestPojo) {
    TimeOffRequest timeOffRequest = requestPojo.getTimeOffRequest(this.getUser());
    TimeOffPolicy policy = timeOffPolicyService.getTimeOffPolicyById(requestPojo.getPolicy());
    timeOffRequest.setTimeOffPolicy(policy);
    timeOffRequest.setTimeOffApprovalStatus(TimeOffRequestApprovalStatus.NO_ACTION);
    TimeOffRequest timeOffRequestReturned = timeOffRequestService
        .createTimeOffRequest(timeOffRequest);

    List<TimeOffRequestDate> timeOffRequestDates = requestPojo
        .getTimeOffRequestDates(timeOffRequestReturned);
    timeOffRequestDateService.saveAllTimeOffRequestDates(timeOffRequestDates);

    timeOffPolicyService
        .updateTimeOffBalance(requestPojo.getPolicyUser(), requestPojo.getTotalHours());
  }

  @GetMapping("time-off_requests/approver/status/no-action/count")
  public Integer getTimeOffRequest() {

    return timeOffRequestService.getCountByApproverAndStatusIsNoAction(this.getUser());
  }

  @GetMapping("time-off-requests/approver")
  public List<TimeOffRequestDto> getTimeOffRequestsByApprover(
      @RequestParam TimeOffRequestApprovalStatus[] status) {

    return timeOffRequestService.getByApproverAndStatus(this.getUser(), status).stream()
        .map(TimeOffRequestDto::new).collect(Collectors.toList());
  }

  @GetMapping("time-off-requests")
  public List<TimeOffRequestDto> getTimeOffRequests(
      @RequestParam TimeOffRequestApprovalStatus[] status) {

    return timeOffRequestService.getRequestsByUserAndStatus(this.getUser(), status).stream()
        .map(TimeOffRequestDto::new).collect(Collectors.toList());
  }


  @PatchMapping("time-off-requests/{id}")
  public HttpEntity updateTimeOffRequest(@PathVariable @HashidsFormat Long id) {

    return null;
  }

  @GetMapping(value = "time-off-requests/requester/{id}")
  public List<TimeOffRequestDto> getMyTimeOffRequests(@HashidsFormat @PathVariable Long id) {
    return timeOffRequestService.getMyTimeOffRequestsByRequesterUserId(id).stream()
        .map(TimeOffRequestDto::new).collect(Collectors.toList());
  }

  @GetMapping(value = "users/{id}/time-off-histories")
  public List<TimeOffRequestDto> getTimeOffHistories(@RequestParam(required = false) Long startTime,
      @RequestParam(required = false) Long endTime, @HashidsFormat @PathVariable Long id) {
    List<TimeOffRequest> timeOffRequests = timeOffRequestService
        .getTimeOffHistories(id, startTime, endTime);
    return timeOffRequests.stream().map(TimeOffRequestDto::new).collect(Collectors.toList());
  }
}
