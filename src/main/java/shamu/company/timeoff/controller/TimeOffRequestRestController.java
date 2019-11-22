package shamu.company.timeoff.controller;

import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.AWAITING_REVIEW;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.DENIED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.NO_ACTION;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.VIEWED;

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
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.timeoff.dto.MyTimeOffDto;
import shamu.company.timeoff.dto.TimeOffRequestCreateDto;
import shamu.company.timeoff.dto.TimeOffRequestDetailDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestUpdateDto;
import shamu.company.timeoff.dto.UnimplementedRequestDto;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.entity.mapper.TimeOffRequestMapper;
import shamu.company.timeoff.pojo.TimeOffRequestStatusPojo;
import shamu.company.timeoff.repository.TimeOffRequestApprovalStatusRepository;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.timeoff.service.TimeOffRequestDateService;
import shamu.company.timeoff.service.TimeOffRequestEmailService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.timeoff.service.TimeOffRequestService.SortFields;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;
import shamu.company.utils.ReflectionUtil;

@RestApiController
public class TimeOffRequestRestController extends BaseRestController {

  private final TimeOffRequestService timeOffRequestService;

  private final TimeOffRequestEmailService timeOffRequestEmailService;

  private final TimeOffRequestDateService timeOffRequestDateService;

  private final UserService userService;

  private final TimeOffRequestMapper timeOffRequestMapper;

  private final TimeOffRequestRepository timeOffRequestRepository;

  private final TimeOffRequestApprovalStatusRepository approvalStatusRepository;

  @Autowired
  public TimeOffRequestRestController(
      final TimeOffRequestService timeOffRequestService,
      final TimeOffRequestEmailService timeOffRequestEmailService,
      final TimeOffRequestDateService timeOffRequestDateService,
      final UserService userService,
      final TimeOffRequestMapper timeOffRequestMapper,
      final TimeOffRequestRepository timeOffRequestRepository,
      final TimeOffRequestApprovalStatusRepository approvalStatusRepository) {
    this.timeOffRequestService = timeOffRequestService;
    this.timeOffRequestEmailService = timeOffRequestEmailService;
    this.timeOffRequestDateService = timeOffRequestDateService;
    this.userService = userService;
    this.timeOffRequestMapper = timeOffRequestMapper;
    this.timeOffRequestRepository = timeOffRequestRepository;
    this.approvalStatusRepository = approvalStatusRepository;
  }

  @PostMapping("users/{userId}/time-off-requests")
  @PreAuthorize("hasPermission(#userId,'USER','MANAGE_SELF_TIME_OFF_REQUEST')")
  public void createTimeOffRequest(
      @PathVariable final String userId,
      @RequestBody final TimeOffRequestCreateDto requestCreateDto) {
    final User user = userService.findUserById(userId);
    final TimeOffRequest timeOffRequest = requestCreateDto.getTimeOffRequest(user);
    timeOffRequest.setApprover(user.getManagerUser());

    final TimeOffRequest timeOffRequestReturned = timeOffRequestService
        .saveTimeOffRequest(
            timeOffRequest, requestCreateDto.getPolicyId(), AWAITING_REVIEW);
    saveTimeOffRequestDates(requestCreateDto, timeOffRequestReturned);

    timeOffRequestEmailService.sendEmail(timeOffRequestReturned);
  }

  @PostMapping("users/{userId}/time-off-requests/approved")
  @PreAuthorize("hasPermission(#userId,'TIME_OFF_REQUEST','CREATE_AND_APPROVED_TIME_OFF_REQUEST')")
  public void createTimeOffRequestAndApproved(
      @PathVariable final String userId,
      @RequestBody final TimeOffRequestCreateDto requestCreateDto) {
    final User user = userService.findUserById(userId);
    final TimeOffRequest timeOffRequest = requestCreateDto.getTimeOffRequest(user);
    final User approver = new User(getAuthUser().getId());
    timeOffRequest.setApproverUser(approver);
    timeOffRequest.setApprover(approver);
    timeOffRequest.setApprovedDate(Timestamp.from(Instant.now()));

    final TimeOffRequest timeOffRequestReturned = timeOffRequestService
        .saveTimeOffRequest(timeOffRequest, requestCreateDto.getPolicyId(), APPROVED);

    saveTimeOffRequestDates(requestCreateDto, timeOffRequestReturned);
  }

  @GetMapping("time-off_requests/approver/status/pending/count")
  @PreAuthorize("hasAuthority('MANAGE_TIME_OFF_REQUEST')")
  public Integer getPendingTimeOffRequestsCount() {

    return timeOffRequestService.getPendingRequestsCount(new User(getAuthUser().getId()));
  }

  @GetMapping("users/{id}/time-off-requests")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_TEAM_TIME_OFF_REQUEST')")
  public List<TimeOffRequestDto> getTimeOffRequests(
      @PathVariable final String id,
      @RequestParam final TimeOffApprovalStatus[] status) {
    return timeOffRequestService.getTimeOffRequest(id, status);
  }

  @GetMapping("time-off-requests/{id}")
  @PreAuthorize(
      "hasPermission(#id,'TIME_OFF_REQUEST','MANAGE_SELF_TIME_OFF_REQUEST') "
          + "or hasPermission(#id,'TIME_OFF_REQUEST','MANAGE_TIME_OFF_REQUEST')")
  public TimeOffRequestDetailDto getTimeOffRequest(@PathVariable final String id) {

    return timeOffRequestService.getTimeOffRequestDetail(id, getAuthUser().getId());
  }


  @PatchMapping("time-off-requests/{id}")
  @PreAuthorize("hasPermission(#id,'TIME_OFF_REQUEST','MANAGE_TIME_OFF_REQUEST')")
  public TimeOffRequestDto updateTimeOffRequestStatus(
      @PathVariable final String id,
      @RequestBody final TimeOffRequestUpdateDto updateDto) {
    return timeOffRequestService.updateTimeOffRequestStatus(id, updateDto, getAuthUser());
  }

  private PageImpl<TimeOffRequestDto> getTimeOffRequestsByApprover(
      final int page, final int size, final String[] statuses, final String sortField) {
    final PageRequest request = PageRequest.of(page, size, Sort.by(sortField).descending());
    final Timestamp startDayTimestamp = DateUtil.getFirstDayOfCurrentYear();

    final Page<TimeOffRequest> timeOffRequests = timeOffRequestService
        .getByApproverAndStatusFilteredByStartDay(
          getAuthUser().getId(), statuses, startDayTimestamp, request);

    return (PageImpl<TimeOffRequestDto>) timeOffRequests
        .map(timeOffRequestMapper::convertToTimeOffRequestDto);
  }

  @GetMapping("time-off-pending-requests/approver")
  @PreAuthorize("hasAuthority('MANAGE_TIME_OFF_REQUEST')")
  public PageImpl<TimeOffRequestDto> getPendingRequestsByApprover(final int page,
      @RequestParam(defaultValue = "5", required = false) final int size) {
    final String[] statuses = new String[]{AWAITING_REVIEW.name()};
    return getTimeOffRequestsByApprover(page, size, statuses, SortFields.CREATED_AT.getValue());
  }

  @GetMapping("time-off-reviewed-requests/approver")
  @PreAuthorize("hasAuthority('MANAGE_TIME_OFF_REQUEST')")
  public PageImpl<TimeOffRequestDto> getReviewedRequestsByApprover(final int page,
      @RequestParam(defaultValue = "5", required = false) final int size) {
    final String[] statuses = new String[]{APPROVED.name(), DENIED.name()};
    return getTimeOffRequestsByApprover(page, size, statuses, SortFields.APPROVED_DATE.getValue());
  }

  @GetMapping(value = "time-off-pending-requests/requester/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER','MANAGE_SELF_TIME_OFF_REQUEST') "
          + "or hasPermission(#id,'USER','MANAGE_TIME_OFF_REQUEST')")
  public MyTimeOffDto getPendingRequests(
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
  public MyTimeOffDto getReviewedRequests(
      @PathVariable(name = "id") final String id,
      @RequestParam(value = "startDay") @Nullable final Long startDay,
      @RequestParam(value = "endDay") @Nullable final Long endDay,
      final int page,
      @RequestParam(defaultValue = "20", required = false) final int size) {
    return timeOffRequestService.getReviewedRequests(id, startDay, endDay, page, size);
  }

  @GetMapping(value = "time-off-requests/approved-after-now/requester/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER','MANAGE_SELF_TIME_OFF_REQUEST') "
          + "or hasPermission(#id,'USER','MANAGE_TIME_OFF_REQUEST')")
  public TimeOffRequestDto getMyTimeOffRequests(
      @PathVariable(name = "id") final String id) {

    final TimeOffRequestDto timeOffRequestDto;
    final Timestamp startDayTimestamp = new Timestamp(new Date().getTime());
    timeOffRequestDto = timeOffRequestService
        .getRecentApprovedRequestByRequesterUserId(
            id, startDayTimestamp, APPROVED.name());
    return timeOffRequestDto;
  }

  private void saveTimeOffRequestDates(
      final TimeOffRequestCreateDto requestCreateDto, final TimeOffRequest timeOffRequest) {
    final List<TimeOffRequestDate> timeOffRequestDates =
        requestCreateDto.getTimeOffRequestDates(timeOffRequest);
    timeOffRequest.setTimeOffRequestDates(new HashSet<>(timeOffRequestDates));
    timeOffRequestDateService.saveAllTimeOffRequestDates(timeOffRequestDates);
  }

  @DeleteMapping("time-off-requests/{requestId}/unimplemented-request")
  @PreAuthorize("(hasPermission(#requestId,'TIME_OFF_REQUEST','MANAGE_TIME_OFF_REQUEST') "
      + "and hasPermission(#unimplementedRequestDto.userId,'USER','EDIT_USER'))"
      + "or (hasPermission(#requestId,'TIME_OFF_REQUEST','MANAGE_SELF_TIME_OFF_REQUEST')"
      + "and hasPermission(#unimplementedRequestDto.userId, 'USER', 'EDIT_SELF'))")
  // TODO remove UnimplementedRequestDto
  public void deleteUnimplementedRequest(
      @PathVariable final String requestId,
      @RequestBody final UnimplementedRequestDto unimplementedRequestDto) {
    timeOffRequestService.deleteUnimplementedRequest(requestId);
  }

  @GetMapping("time-off-request/has-privilege/user/{id}")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_TEAM_TIME_OFF_REQUEST')")
  public boolean hasUserPermission(@PathVariable final String id) {
    final User currentUser = userService.findByUserId(getUserId());
    final Role userRole = currentUser.getRole();
    final User targetUser = userService.findUserById(id);
    if (getAuthUser().getId().equals(targetUser.getId())) {
      return targetUser.getManagerUser() == null;
    } else {
      return userRole == User.Role.ADMIN || currentUser.equals(targetUser.getManagerUser());
    }
  }

  @GetMapping("time-off-request/test/{id}")
  public List<TimeOffRequestStatusPojo> getTimeOffRequestPolicy(@PathVariable String id) {
    return timeOffRequestRepository.findByTimeOffPolicyId(id);
  }

  @GetMapping("time-off-approval-statuses")
  public List<CommonDictionaryDto> getTimeOffApprovalStatuses() {
    List<TimeOffRequestApprovalStatus> approvalStatuses =
        approvalStatusRepository.findAll();

    return ReflectionUtil.convertTo(approvalStatuses, CommonDictionaryDto.class);
  }
}
