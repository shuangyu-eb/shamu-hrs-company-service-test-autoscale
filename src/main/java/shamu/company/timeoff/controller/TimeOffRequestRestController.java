package shamu.company.timeoff.controller;

import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.APPROVED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.DENIED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.NO_ACTION;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.VIEWED;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.dto.MyTimeOffDto;
import shamu.company.timeoff.dto.TimeOffRequestDetailDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestUpdateDto;
import shamu.company.timeoff.dto.UnimplementedRequestDto;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestComment;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.entity.mapper.TimeOffRequestMapper;
import shamu.company.timeoff.pojo.TimeOffRequestPojo;
import shamu.company.timeoff.service.TimeOffRequestDateService;
import shamu.company.timeoff.service.TimeOffRequestEmailService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;
import shamu.company.utils.DateUtil;

@RestApiController
public class TimeOffRequestRestController extends BaseRestController {

  private final TimeOffRequestService timeOffRequestService;

  private final TimeOffRequestEmailService timeOffRequestEmailService;

  private final TimeOffRequestDateService timeOffRequestDateService;

  private final UserService userService;

  private final TimeOffRequestMapper timeOffRequestMapper;

  private final Auth0Util auth0Util;

  @Autowired
  public TimeOffRequestRestController(
      final TimeOffRequestService timeOffRequestService,
      final TimeOffRequestEmailService timeOffRequestEmailService,
      final TimeOffRequestDateService timeOffRequestDateService,
      final UserService userService,
      final TimeOffRequestMapper timeOffRequestMapper,
      final Auth0Util auth0Util) {
    this.timeOffRequestService = timeOffRequestService;
    this.timeOffRequestEmailService = timeOffRequestEmailService;
    this.timeOffRequestDateService = timeOffRequestDateService;
    this.userService = userService;
    this.timeOffRequestMapper = timeOffRequestMapper;
    this.auth0Util = auth0Util;
  }

  @PostMapping("users/{userId}/time-off-requests")
  @PreAuthorize("hasPermission(#userId,'USER','MANAGE_SELF_TIME_OFF_REQUEST')")
  public void createTimeOffRequest(
      @PathVariable @HashidsFormat final Long userId,
      @RequestBody final TimeOffRequestPojo requestPojo) {
    final User user = userService.findUserById(userId);
    final TimeOffRequest timeOffRequest = requestPojo.getTimeOffRequest(user);
    timeOffRequest.setApprover(user.getManagerUser());

    final TimeOffRequest timeOffRequestReturned = timeOffRequestService
        .saveTimeOffRequest(
            timeOffRequest, requestPojo.getPolicyId(), TimeOffRequestApprovalStatus.NO_ACTION);
    saveTimeOffRequestDates(requestPojo, timeOffRequestReturned);

    timeOffRequestEmailService.sendEmail(timeOffRequestReturned);
  }

  @PostMapping("users/{userId}/time-off-requests/approved")
  @PreAuthorize("hasPermission(#userId,'TIME_OFF_REQUEST','CREATE_AND_APPROVED_TIME_OFF_REQUEST')")
  public void createTimeOffRequestAndApproved(
      @PathVariable @HashidsFormat final Long userId,
      @RequestBody final TimeOffRequestPojo requestPojo) {
    final User user = userService.findUserById(userId);
    final TimeOffRequest timeOffRequest = requestPojo.getTimeOffRequest(user);
    final User approver = new User(getAuthUser().getId());
    timeOffRequest.setApproverUser(approver);
    timeOffRequest.setApprover(approver);
    timeOffRequest.setApprovedDate(Timestamp.from(Instant.now()));

    final TimeOffRequest timeOffRequestReturned = timeOffRequestService
        .saveTimeOffRequest(timeOffRequest, requestPojo.getPolicyId(), APPROVED);

    saveTimeOffRequestDates(requestPojo, timeOffRequestReturned);
  }

  @GetMapping("time-off_requests/approver/status/pending/count")
  @PreAuthorize("hasAuthority('MANAGE_TIME_OFF_REQUEST')")
  public Integer getPendingTimeOffRequestsCount() {

    return timeOffRequestService.getPendingRequestsCount(new User(getAuthUser().getId()));
  }

  @GetMapping("users/{id}/time-off-requests")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_TEAM_TIME_OFF_REQUEST')")
  public List<TimeOffRequestDto> getTimeOffRequests(
      @PathVariable @HashidsFormat final Long id,
      @RequestParam final TimeOffRequestApprovalStatus[] status) {
    return timeOffRequestService.getTimeOffRequest(id, status, getAuthUser());
  }

  @GetMapping("time-off-requests/{id}")
  @PreAuthorize(
      "hasPermission(#id,'TIME_OFF_REQUEST','MANAGE_SELF_TIME_OFF_REQUEST') "
          + "or hasPermission(#id,'TIME_OFF_REQUEST','MANAGE_TIME_OFF_REQUEST')")
  public TimeOffRequestDetailDto getTimeOffRequest(@PathVariable @HashidsFormat final Long id) {

    return timeOffRequestService.getTimeOffRequestDetail(id, getAuthUser().getId());
  }


  @PatchMapping("time-off-requests/{id}")
  @PreAuthorize("hasPermission(#id,'TIME_OFF_REQUEST','MANAGE_TIME_OFF_REQUEST')")
  public TimeOffRequestDto updateTimeOffRequestStatus(
      @PathVariable @HashidsFormat final Long id,
      @RequestBody final TimeOffRequestUpdateDto updateDto) {

    TimeOffRequest timeOffRequest = timeOffRequestMapper
        .createFromTimeOffRequestUpdateDto(updateDto);
    timeOffRequest.setId(id);

    final TimeOffRequestApprovalStatus status = updateDto.getStatus();
    if (status == APPROVED || status == DENIED) {
      timeOffRequest.setApproverUser(new User(getAuthUser().getId()));
      timeOffRequest.setApprovedDate(Timestamp.from(Instant.now()));
    }
    TimeOffRequestComment comment = null;
    if (updateDto.getApproverComment() != null && updateDto.getApproverComment().length() > 0) {
      comment = new TimeOffRequestComment();
      comment.setTimeOffRequestId(id);
      comment.setComment(updateDto.getApproverComment());
      comment.setUser(new User(getAuthUser().getId()));
    }

    timeOffRequest = timeOffRequestService.updateTimeOffRequest(timeOffRequest, comment);

    return timeOffRequestMapper.convertToTimeOffRequestDto(timeOffRequest);
  }

  private PageImpl<TimeOffRequestDto> getTimeOffRequestsByApprover(
      final int page, final int size, final Long[] statusIds, final String sortField) {
    final PageRequest request = PageRequest.of(page, size, Sort.by(sortField).descending());
    final Timestamp startDayTimestamp = DateUtil.getFirstDayOfCurrentYear();

    final Page<TimeOffRequest> timeOffRequests = timeOffRequestService
        .getByApproverAndStatusFilteredByStartDay(
          getAuthUser().getId(), statusIds, startDayTimestamp, request);

    return (PageImpl<TimeOffRequestDto>) timeOffRequests
        .map(timeOffRequestMapper::convertToTimeOffRequestDto);
  }

  @GetMapping("time-off-pending-requests/approver")
  @PreAuthorize("hasAuthority('MANAGE_TIME_OFF_REQUEST')")
  public PageImpl<TimeOffRequestDto> getPendingRequestsByApprover(final int page,
      @RequestParam(defaultValue = "5", required = false) final int size) {
    final Long[] statusIds = new Long[]{NO_ACTION.getValue(), VIEWED.getValue()};
    return getTimeOffRequestsByApprover(page, size, statusIds, SortFields.CREATED_AT.getValue());
  }

  @GetMapping("time-off-reviewed-requests/approver")
  @PreAuthorize("hasAuthority('MANAGE_TIME_OFF_REQUEST')")
  public PageImpl<TimeOffRequestDto> getReviewedRequestsByApprover(final int page,
      @RequestParam(defaultValue = "5", required = false) final int size) {
    final Long[] statusIds = new Long[]{APPROVED.getValue(), DENIED.getValue()};
    return getTimeOffRequestsByApprover(page, size, statusIds, SortFields.APPROVED_DATE.getValue());
  }

  @GetMapping(value = "time-off-pending-requests/requester/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER','MANAGE_SELF_TIME_OFF_REQUEST') "
          + "or hasPermission(#id,'USER','MANAGE_TIME_OFF_REQUEST')")
  public MyTimeOffDto getPendingRequests(
      @HashidsFormat @PathVariable(name = "id") final Long id, final int page,
      @RequestParam(defaultValue = "5", required = false) final int size) {

    final PageRequest request = PageRequest.of(
        page, size, Sort.by(SortFields.CREATED_AT.getValue()).descending());
    final MyTimeOffDto myTimeOffDto;
    final Timestamp startDayTimestamp = DateUtil.getFirstDayOfCurrentYear();
    final Long[] timeOffRequestStatuses = new Long[]{NO_ACTION.getValue(), VIEWED.getValue()};

    myTimeOffDto =
        timeOffRequestService.getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
            id, startDayTimestamp, timeOffRequestStatuses, request);

    return myTimeOffDto;
  }

  @GetMapping(value = "time-off-reviewed-requests/requester/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER','MANAGE_SELF_TIME_OFF_REQUEST') "
          + "or hasPermission(#id,'USER','MANAGE_TIME_OFF_REQUEST')")
  public MyTimeOffDto getReviewedRequests(
      @HashidsFormat @PathVariable(name = "id") final Long id,
      @RequestParam(value = "startDay") @Nullable final Long startDay,
      @RequestParam(value = "endDay") @Nullable final Long endDay,
      final int page,
      @RequestParam(defaultValue = "20", required = false) final int size) {

    final PageRequest request = PageRequest.of(
        page, size, Sort.by(SortFields.APPROVED_DATE.getValue()).descending());
    final MyTimeOffDto myTimeOffDto;
    final Timestamp startDayTimestamp;
    final Long[] timeOffRequestStatuses = new Long[]{APPROVED.getValue(), DENIED.getValue()};

    if (startDay == null) {
      startDayTimestamp = DateUtil.getFirstDayOfCurrentYear();
    } else {
      startDayTimestamp = new Timestamp(startDay);
    }

    if (endDay != null) {
      myTimeOffDto =
          timeOffRequestService.getMyTimeOffRequestsByRequesterUserIdFilteredByStartAndEndDay(
              id, startDayTimestamp, new Timestamp(endDay), timeOffRequestStatuses, request);
    } else {
      myTimeOffDto =
          timeOffRequestService.getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
              id, startDayTimestamp, timeOffRequestStatuses, request);
    }

    return myTimeOffDto;
  }

  @GetMapping(value = "time-off-requests/approved-after-now/requester/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER','MANAGE_SELF_TIME_OFF_REQUEST') "
          + "or hasPermission(#id,'USER','MANAGE_TIME_OFF_REQUEST')")
  public TimeOffRequestDto getMyTimeOffRequests(
      @HashidsFormat @PathVariable(name = "id") final Long id) {

    final TimeOffRequestDto timeOffRequestDto;
    final Timestamp startDayTimestamp = new Timestamp(new Date().getTime());
    timeOffRequestDto = timeOffRequestService
        .getRecentApprovedRequestByRequesterUserId(
            id, startDayTimestamp, APPROVED.getValue());
    return timeOffRequestDto;
  }

  private void saveTimeOffRequestDates(
      final TimeOffRequestPojo requestPojo, final TimeOffRequest timeOffRequest) {
    final List<TimeOffRequestDate> timeOffRequestDates =
        requestPojo.getTimeOffRequestDates(timeOffRequest);
    timeOffRequest.setTimeOffRequestDates(new HashSet<>(timeOffRequestDates));
    timeOffRequestDateService.saveAllTimeOffRequestDates(timeOffRequestDates);
  }

  @DeleteMapping("time-off-requests/{requestId}/unimplemented-request")
  @PreAuthorize("(hasPermission(#requestId,'TIME_OFF_REQUEST','MANAGE_TIME_OFF_REQUEST') "
      + "and hasPermission(#unimplementedRequestDto.userId,'USER','EDIT_USER'))"
      + "or (hasPermission(#requestId,'TIME_OFF_REQUEST','MANAGE_SELF_TIME_OFF_REQUEST')"
      + "and hasPermission(#unimplementedRequestDto.userId, 'USER', 'EDIT_SELF'))")
  public void deleteUnimplementedRequest(
      @PathVariable @HashidsFormat final Long requestId,
      @RequestBody final UnimplementedRequestDto unimplementedRequestDto) {
    timeOffRequestService.deleteUnimplementedRequest(requestId, unimplementedRequestDto);
  }

  private enum SortFields {
    CREATED_AT("created_at"),
    APPROVED_DATE("approved_date");

    String value;

    SortFields(final String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  @GetMapping("time-off-request/has-privilege/user/{id}")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_TEAM_TIME_OFF_REQUEST')")
  public boolean hasUserPermission(@HashidsFormat @PathVariable final Long id) {
    final User.Role userRole = auth0Util.getUserRole(getUserId());
    final User targetUser = userService.findUserById(id);
    if (getAuthUser().getId() == targetUser.getId()) {
      return targetUser.getManagerUser() == null;
    } else {
      return userRole == User.Role.ADMIN;
    }
  }
}
