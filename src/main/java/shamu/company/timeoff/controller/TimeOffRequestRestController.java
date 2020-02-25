package shamu.company.timeoff.controller;

import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.AWAITING_REVIEW;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.DENIED;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.timeoff.dto.MyTimeOffDto;
import shamu.company.timeoff.dto.TimeOffRequestCreateDto;
import shamu.company.timeoff.dto.TimeOffRequestDetailDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestUpdateDto;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.timeoff.service.TimeOffRequestService.SortFields;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;

@RestApiController
public class TimeOffRequestRestController extends BaseRestController {

  private final TimeOffRequestService timeOffRequestService;

  private final UserService userService;

  @Autowired
  public TimeOffRequestRestController(
      final TimeOffRequestService timeOffRequestService,
      final UserService userService) {
    this.timeOffRequestService = timeOffRequestService;
    this.userService = userService;
  }

  @PostMapping("users/{userId}/time-off-requests")
  @PreAuthorize("hasPermission(#userId,'USER','MANAGE_SELF_TIME_OFF_REQUEST')")
  public HttpEntity createTimeOffRequest(
      @PathVariable final String userId,
      @RequestBody final TimeOffRequestCreateDto requestCreateDto) {
    final User user = userService.findById(userId);
    final TimeOffRequest timeOffRequest = requestCreateDto.getTimeOffRequest(user);
    timeOffRequest.setApproverUser(user.getManagerUser());

    timeOffRequestService.saveTimeOffRequest(
            timeOffRequest, requestCreateDto, AWAITING_REVIEW);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("users/{userId}/time-off-requests/approved")
  @PreAuthorize("hasPermission(#userId,'TIME_OFF_REQUEST','CREATE_AND_APPROVED_TIME_OFF_REQUEST')")
  public HttpEntity createTimeOffRequestAndApprove(
      @PathVariable final String userId,
      @RequestBody final TimeOffRequestCreateDto requestCreateDto) {
    final User user = userService.findById(userId);
    final TimeOffRequest timeOffRequest = requestCreateDto.getTimeOffRequest(user);
    final User approver = new User(findAuthUser().getId());

    timeOffRequestService.saveTimeOffRequest(timeOffRequest, requestCreateDto, APPROVED, approver);

    return new ResponseEntity<>(HttpStatus.OK);

  }

  @GetMapping("time-off_requests/approver/status/pending/count")
  @PreAuthorize("hasAuthority('MANAGE_TIME_OFF_REQUEST')")
  public Integer getPendingTimeOffRequestsCount() {

    return timeOffRequestService.getPendingRequestsCount(new User(findAuthUser().getId()));
  }

  @GetMapping("users/{id}/time-off-requests")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_TEAM_TIME_OFF_REQUEST')")
  public List<TimeOffRequestDto> findTimeOffRequests(
      @PathVariable final String id,
      @RequestParam final TimeOffApprovalStatus[] status) {
    return timeOffRequestService.findTimeOffRequests(id, status);
  }

  @GetMapping("time-off-requests/{id}")
  @PreAuthorize(
      "hasPermission(#id,'TIME_OFF_REQUEST','MANAGE_SELF_TIME_OFF_REQUEST') "
          + "or hasPermission(#id,'TIME_OFF_REQUEST','MANAGE_TIME_OFF_REQUEST')")
  public TimeOffRequestDetailDto findTimeOffRequest(@PathVariable final String id) {

    return timeOffRequestService
        .findTimeOffRequestDetail(id, findAuthUser());
  }


  @PatchMapping("time-off-requests/{id}")
  @PreAuthorize("hasPermission(#id,'TIME_OFF_REQUEST','MANAGE_TIME_OFF_REQUEST')")
  public TimeOffRequestDto updateTimeOffRequestStatus(
      @PathVariable final String id,
      @RequestBody final TimeOffRequestUpdateDto updateDto) {
    return timeOffRequestService.updateTimeOffRequestStatus(id, updateDto, findAuthUser());
  }

  @GetMapping("time-off-pending-requests/approver")
  @PreAuthorize("hasAuthority('MANAGE_TIME_OFF_REQUEST')")
  public PageImpl<TimeOffRequestDto> findPendingRequestsByApprover(final int page,
      @RequestParam(defaultValue = "5", required = false) final int size) {
    final String[] statuses = new String[]{AWAITING_REVIEW.name()};
    final PageRequest request = PageRequest.of(page, size,
        Sort.by(SortFields.CREATED_AT.getValue()).descending());
    return timeOffRequestService
        .findRequestsByApproverAndStatuses(request, statuses,findAuthUser());
  }

  @GetMapping("time-off-reviewed-requests/approver")
  @PreAuthorize("hasAuthority('MANAGE_TIME_OFF_REQUEST')")
  public PageImpl<TimeOffRequestDto> findReviewedRequestsByApprover(final int page,
      @RequestParam(defaultValue = "5", required = false) final int size) {
    final String[] statuses = new String[]{APPROVED.name(), DENIED.name()};
    final PageRequest request = PageRequest.of(page, size,
        Sort.by(SortFields.APPROVED_DATE.getValue()).descending());
    return timeOffRequestService
        .findRequestsByApproverAndStatuses(request, statuses,findAuthUser());
  }

  @GetMapping(value = "time-off-pending-requests/requester/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER','MANAGE_SELF_TIME_OFF_REQUEST') "
          + "or hasPermission(#id,'USER','MANAGE_TIME_OFF_REQUEST')")
  public MyTimeOffDto findPendingRequests(
      @PathVariable(name = "id") final String id, final int page,
      @RequestParam(defaultValue = "5", required = false) final int size) {

    final PageRequest request = PageRequest.of(
        page, size, Sort.by(SortFields.CREATED_AT.getValue()).descending());
    final MyTimeOffDto myTimeOffDto;
    final Timestamp startDayTimestamp = DateUtil.getFirstDayOfCurrentYear();
    final String[] timeOffRequestStatuses = new String[]{AWAITING_REVIEW.name()};

    myTimeOffDto =
        timeOffRequestService.getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
            id, startDayTimestamp, timeOffRequestStatuses, request);

    return myTimeOffDto;
  }

  @GetMapping(value = "time-off-reviewed-requests/requester/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER','MANAGE_SELF_TIME_OFF_REQUEST') "
          + "or hasPermission(#id,'USER','MANAGE_TIME_OFF_REQUEST')")
  public MyTimeOffDto findReviewedRequests(
      @PathVariable(name = "id") final String id,
      @RequestParam(value = "startDay") @Nullable final Long startDay,
      @RequestParam(value = "endDay") @Nullable final Long endDay,
      final int page,
      @RequestParam(defaultValue = "20", required = false) final int size) {
    return timeOffRequestService.findReviewedRequests(id, startDay, endDay, page, size);
  }

  @GetMapping(value = "time-off-requests/approved-after-now/requester/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER','MANAGE_SELF_TIME_OFF_REQUEST') "
          + "or hasPermission(#id,'USER','MANAGE_TIME_OFF_REQUEST')")
  public TimeOffRequestDto findMyTimeOffRequests(
      @PathVariable(name = "id") final String id) {

    final TimeOffRequestDto timeOffRequestDto;
    final Timestamp startDayTimestamp = new Timestamp(new Date().getTime());
    timeOffRequestDto = timeOffRequestService
        .findRecentRequestByRequesterAndStatus(
            id, startDayTimestamp, APPROVED.name());
    return timeOffRequestDto;
  }

  @DeleteMapping("time-off-requests/{requestId}/unimplemented-requests/{userId}")
  @PreAuthorize("(hasPermission(#requestId,'TIME_OFF_REQUEST','MANAGE_TIME_OFF_REQUEST') "
      + "and hasPermission(#userId,'USER','EDIT_USER'))"
      + "or (hasPermission(#requestId,'TIME_OFF_REQUEST','MANAGE_SELF_TIME_OFF_REQUEST')"
      + "and hasPermission(#userId, 'USER', 'EDIT_SELF'))")
  public void deleteUnimplementedRequest(
      @PathVariable final String requestId,
      @PathVariable final String userId) {
    timeOffRequestService.deleteUnimplementedRequest(requestId);
  }

  @GetMapping("time-off-requests/has-privilege/users/{id}")
  public boolean hasUserPermission(@PathVariable final String id) {
    final User currentUser = userService.findById(findUserId());
    final Role userRole = currentUser.getRole();
    final User targetUser = userService.findById(id);
    if (userRole == Role.INACTIVATE) {
      return false;
    }
    if (findAuthUser().getId().equals(targetUser.getId())) {
      return targetUser.getManagerUser() == null;
    }
    return userRole == User.Role.ADMIN || currentUser.equals(targetUser.getManagerUser());
  }

}
