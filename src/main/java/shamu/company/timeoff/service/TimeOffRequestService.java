package shamu.company.timeoff.service;

import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.AWAITING_REVIEW;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.DENIED;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.server.dto.AuthUser;
import shamu.company.timeoff.dto.BasicTimeOffRequestDto;
import shamu.company.timeoff.dto.MyTimeOffDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffRequestCreateDto;
import shamu.company.timeoff.dto.TimeOffRequestDetailDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestUpdateDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestComment;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.entity.mapper.TimeOffRequestMapper;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;
import shamu.company.utils.UuidUtil;

@Service
public class TimeOffRequestService {

  private final TimeOffRequestRepository timeOffRequestRepository;

  private final TimeOffPolicyUserService timeOffPolicyUserService;

  private final TimeOffRequestEmailService timeOffRequestEmailService;

  private final TimeOffRequestMapper timeOffRequestMapper;

  private final TimeOffPolicyService timeOffPolicyService;

  private final TimeOffDetailService timeOffDetailService;

  private final Auth0Helper auth0Helper;

  private final UserService userService;


  private final TimeOffRequestApprovalStatusService timeOffRequestApprovalStatusService;

  private final TimeOffRequestDateService timeOffRequestDateService;


  @Autowired
  public TimeOffRequestService(
      final TimeOffRequestRepository timeOffRequestRepository,
      @Lazy final TimeOffRequestEmailService timeOffRequestEmailService,
      final TimeOffRequestMapper timeOffRequestMapper,
      final TimeOffPolicyService timeOffPolicyService,
      final TimeOffDetailService timeOffDetailService,
      final Auth0Helper auth0Helper,
      final UserService userService,
      final TimeOffRequestDateService timeOffRequestDateService,
      final TimeOffPolicyUserService timeOffPolicyUserService,
      final TimeOffRequestApprovalStatusService timeOffRequestApprovalStatusService) {
    this.timeOffRequestRepository = timeOffRequestRepository;
    this.timeOffRequestEmailService = timeOffRequestEmailService;
    this.timeOffRequestMapper = timeOffRequestMapper;
    this.timeOffPolicyService = timeOffPolicyService;
    this.timeOffDetailService = timeOffDetailService;
    this.auth0Helper = auth0Helper;
    this.userService = userService;
    this.timeOffRequestDateService = timeOffRequestDateService;
    this.timeOffPolicyUserService = timeOffPolicyUserService;
    this.timeOffRequestApprovalStatusService = timeOffRequestApprovalStatusService;
  }

  public TimeOffRequest findByRequestId(final String id) {
    return timeOffRequestRepository.findByRequestId(id);
  }

  public Page<TimeOffRequest> getByApproverAndStatusFilteredByStartDay(
      final String id, final String[] statuses,
      final Timestamp startDay, final PageRequest pageRequest) {
    return timeOffRequestRepository.findByApproversAndTimeOffApprovalStatusFilteredByStartDay(
        id, statuses, startDay, pageRequest);
  }

  public Integer getPendingRequestsCount(final User approver) {
    final String status = AWAITING_REVIEW.name();
    return timeOffRequestRepository.countByApproverIdAndTimeOffApprovalStatus(
        approver.getId(), status);
  }

  public TimeOffRequest getById(final String timeOffRequestId) {
    return timeOffRequestRepository
        .findById(timeOffRequestId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("No time off request with id: " + timeOffRequestId));
  }

  public TimeOffRequest save(final TimeOffRequest timeOffRequest) {
    return timeOffRequestRepository.save(timeOffRequest);
  }

  public TimeOffRequest createTimeOffRequest(final TimeOffRequest request) {
    return timeOffRequestRepository.save(request);
  }

  public List<TimeOffRequest> getRequestsByUserAndStatus(
      final User user, final TimeOffApprovalStatus[] status) {

    final List<String> statusNames = Arrays.stream(status)
        .map(TimeOffApprovalStatus::name).collect(Collectors.toList());

    final User.Role userRole = auth0Helper
        .getUserRole(user.getId());

    final List<TimeOffRequest> result;
    final List<TimeOffRequest> selfPendingRequests = timeOffRequestRepository
        .findEmployeeSelfPendingRequests(user.getId(),
            AWAITING_REVIEW.name());
    if (user.getManagerUser() == null) {
      result = timeOffRequestRepository.findAdminTeamRequests(user.getId(), statusNames);
    } else if (userRole == User.Role.MANAGER || userRole == User.Role.ADMIN) {
      result = timeOffRequestRepository.findManagerTeamRequests(
          user.getId(), user.getManagerUser().getId(), statusNames);
    } else {
      result = timeOffRequestRepository.employeeFindTeamRequests(
          user.getManagerUser().getId(), statusNames);
    }
    result.addAll(selfPendingRequests);
    return result;
  }

  private MyTimeOffDto getTimeOffDtos(
      final String id, final Timestamp startDay, final Timestamp endDay,
      final Boolean filteredByEndDay,
      final String[] statuses, final PageRequest request) {
    final MyTimeOffDto myTimeOffDto = new MyTimeOffDto();
    final Boolean policiesAdded = timeOffPolicyUserService.existsByUserId(id);
    myTimeOffDto.setPoliciesAdded(policiesAdded);

    final Page<TimeOffRequest> timeOffRequests;
    if (filteredByEndDay) {
      timeOffRequests =
          timeOffRequestRepository.findByRequesterUserIdFilteredByStartAndEndDay(
              id, startDay, endDay, statuses, request);
    } else {
      timeOffRequests =
          timeOffRequestRepository.findByRequesterUserIdFilteredByStartDay(
              id, startDay, statuses, request);
    }
    final List<TimeOffRequestDto> timeOffRequestDtos =
        timeOffRequests.getContent().stream()
            .map(timeOffRequestMapper::convertToTimeOffRequestDto).collect(Collectors.toList());
    myTimeOffDto.setTimeOffRequests(
        new PageImpl<>(timeOffRequestDtos, request, timeOffRequests.getTotalElements()));

    return myTimeOffDto;
  }

  public TimeOffRequestDto findRecentRequestByRequesterAndStatus(
      final String id, final Timestamp startDay, final String statusName) {
    final TimeOffRequest timeOffRequest = timeOffRequestRepository
        .findRecentApprovedRequestByRequesterUserId(id, startDay, statusName);
    return timeOffRequestMapper.convertToTimeOffRequestDto(timeOffRequest);
  }

  public MyTimeOffDto findReviewedRequests(final String id,
      final Long startDay,
      final Long endDay,
      final int page,
      final int size) {
    final PageRequest request = PageRequest.of(
        page, size, Sort.by(SortFields.APPROVED_DATE.getValue()).descending());
    final MyTimeOffDto myTimeOffDto;
    final Timestamp startDayTimestamp;
    final String[] timeOffRequestStatuses = new String[]{APPROVED.name(), DENIED.name()};

    if (startDay == null) {
      startDayTimestamp = DateUtil.getFirstDayOfCurrentYear();
    } else {
      startDayTimestamp = new Timestamp(startDay);
    }

    if (endDay != null) {
      myTimeOffDto = getMyTimeOffRequestsByRequesterUserIdFilteredByStartAndEndDay(
          id, startDayTimestamp, new Timestamp(endDay), timeOffRequestStatuses, request);
    } else {
      myTimeOffDto = getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
          id, startDayTimestamp, timeOffRequestStatuses, request);
    }

    return myTimeOffDto;
  }

  public MyTimeOffDto getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
      final String id, final Timestamp startDay, final String[] statuses,
      final PageRequest request) {
    return getTimeOffDtos(id, startDay, null, false, statuses, request);
  }

  private MyTimeOffDto getMyTimeOffRequestsByRequesterUserIdFilteredByStartAndEndDay(
      final String id, final Timestamp startDay, final Timestamp endDay,
      final String[] statuses, final PageRequest request) {
    return getTimeOffDtos(id, startDay, endDay, true, statuses, request);
  }

  public List<TimeOffRequest> getOtherRequestsBy(final TimeOffRequest timeOffRequest) {
    Timestamp start = DateUtil.getFirstDayOfCurrentMonth();
    final Timestamp end;
    final User requester = timeOffRequest.getRequesterUser();
    final Timestamp startDay = timeOffRequest.getStartDay();
    if (start.before(startDay)) {
      end = DateUtil.getDayOfNextYear();
    } else {
      start = DateUtil.getFirstDayOfMonth(startDay);
      end = DateUtil.getDayOfNextYear(start);
    }

    final User manager = requester.getManagerUser();
    final List<User> requesters = userService.findDirectReportsByManagerId(requester.getId());

    if (manager != null) {
      requesters.addAll(userService.findDirectReportsByManagerId(manager.getId()));
      requesters.add(manager);
    } else {
      requesters.add(requester);
    }

    final List<byte[]> requesterIds = requesters.stream().map(User::getId)
        .map(UuidUtil::toBytes)
        .collect(Collectors.toList());

    List<TimeOffRequest> timeOffRequests =
        timeOffRequestRepository
            .findByRequesterUserInAndTimeOffApprovalStatus(requesterIds,
                APPROVED.name(), start, end);

    if (timeOffRequest.getApprovalStatus() == APPROVED) {
      timeOffRequests =
          timeOffRequests.stream()
              .filter(request -> !timeOffRequest.getId().equals(request.getId()))
              .collect(Collectors.toList());
    }

    return timeOffRequests;
  }

  public TimeOffRequestDto updateTimeOffRequestStatus(final String id,
      final TimeOffRequestUpdateDto updateDto, final AuthUser user) {
    TimeOffRequest timeOffRequest = timeOffRequestMapper
        .createFromTimeOffRequestUpdateDto(updateDto);
    timeOffRequest.setId(id);

    final TimeOffApprovalStatus status = updateDto.getStatus();
    if (status == APPROVED || status == DENIED) {
      timeOffRequest.setApproverUser(new User(user.getId()));
      timeOffRequest.setApprovedDate(Timestamp.valueOf(DateUtil.getLocalUtcTime()));
    }
    TimeOffRequestComment comment = null;
    if (updateDto.getApproverComment() != null && updateDto.getApproverComment().length() > 0) {
      comment = new TimeOffRequestComment();
      comment.setTimeOffRequestId(id);
      comment.setComment(updateDto.getApproverComment());
      comment.setUser(new User(user.getId()));
    }

    timeOffRequest = updateTimeOffRequest(timeOffRequest, comment);

    return timeOffRequestMapper.convertToTimeOffRequestDto(timeOffRequest);
  }

  //TODO updateTimeOffRequest don't update balance
  private TimeOffRequest updateTimeOffRequest(
      final TimeOffRequest timeOffRequest, final TimeOffRequestComment timeOffRequestComment) {

    TimeOffRequest original = getById(timeOffRequest.getId());
    final TimeOffApprovalStatus status = timeOffRequest.getApprovalStatus();

    final TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
        timeOffRequestApprovalStatusService.findByName(status.name());
    original.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);
    original.setApproverUser(timeOffRequest.getApproverUser());
    original.setApprovedDate(Timestamp.from(Instant.from(DateUtil.getLocalUtcTime())));

    if (timeOffRequestComment != null) {
      original.setComment(timeOffRequestComment);
    }

    original = timeOffRequestRepository.save(original);

    if (status == APPROVED || status == DENIED) {
      timeOffRequestEmailService.sendEmail(original);
    }

    return original;
  }

  @Transactional
  public void deleteUnimplementedRequest(final String requestId) {
    timeOffRequestRepository.delete(requestId);
  }


  public TimeOffRequestDetailDto findTimeOffRequestDetail(final String id,
      final AuthUser currentUser) {
    final TimeOffRequest timeOffRequest = getById(id);
    final User requester = timeOffRequest.getRequesterUser();
    final TimeOffRequestDetailDto requestDetail = timeOffRequestMapper
        .convertToTimeOffRequestDetailDto(timeOffRequest);

    final List<BasicTimeOffRequestDto> timeOffRequests =
        getOtherRequestsBy(timeOffRequest).stream()
            .map(timeOffRequestMapper::convertToBasicTimeOffRequestDto)
            .collect(Collectors.toList());
    requestDetail.setOtherTimeOffRequests(timeOffRequests);

    setTimeOffDetailDtoIsCurrentUserPrivileged(requester,currentUser,requestDetail);

    return requestDetail;
  }

  private void setTimeOffDetailDtoIsCurrentUserPrivileged(final User requester,
      final AuthUser currentUser,
      final TimeOffRequestDetailDto timeOffRequestDetailDto) {
    if (currentUser.getRole() == Role.ADMIN
        || (requester.getManagerUser() != null
        && requester.getManagerUser().getId().equals(currentUser.getId()))) {
      timeOffRequestDetailDto.setIsCurrentUserPrivileged(true);
    }
  }

  public void saveTimeOffRequest(TimeOffRequest timeOffRequest,
      TimeOffRequestCreateDto requestCreateDto, TimeOffApprovalStatus status, User approver) {
    timeOffRequest.setApproverUser(approver);

    timeOffRequest.setApprovedDate(Timestamp.from(Instant.now()));

    final TimeOffPolicy policy = timeOffPolicyService
        .getTimeOffPolicyById(requestCreateDto.getPolicyId());
    timeOffRequest.setTimeOffPolicy(policy);

    saveTimeOffRequest(timeOffRequest,requestCreateDto,status);

  }

  public void saveTimeOffRequest(
      final TimeOffRequest timeOffRequest, final TimeOffRequestCreateDto requestCreateDto,
      final TimeOffApprovalStatus status) {

    final TimeOffPolicy policy = timeOffPolicyService
        .getTimeOffPolicyById(requestCreateDto.getPolicyId());
    timeOffRequest.setTimeOffPolicy(policy);

    final TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
        timeOffRequestApprovalStatusService.findByName(status.name());
    timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);
    final TimeOffPolicyUser timeOffPolicyUser = timeOffPolicyUserService
        .findByUserAndTimeOffPolicy(timeOffRequest.getRequesterUser(), policy);

    final Integer approvalBalance = this.calApprovalBalance(timeOffPolicyUser);

    timeOffRequest.setBalance(approvalBalance);

    final TimeOffRequest timeOffRequestReturned = timeOffRequestRepository.save(timeOffRequest);

    this.saveTimeOffRequestDates(requestCreateDto, timeOffRequest);

    if (status != APPROVED) {
      timeOffRequestEmailService.sendEmail(timeOffRequestReturned);
    }

  }

  private Integer calApprovalBalance(final TimeOffPolicyUser timeOffPolicyUser) {
    final LocalDateTime currentTime = LocalDateTime.now();
    final TimeOffBreakdownDto timeOffBreakdownDto = timeOffDetailService
        .getTimeOffBreakdown(timeOffPolicyUser.getId(), null);
    final Integer balance = timeOffBreakdownDto.getBalance();
    final Integer approvedHours = timeOffPolicyService.getTimeOffRequestHoursFromStatus(
        timeOffPolicyUser.getUser().getId(),
        timeOffPolicyUser.getTimeOffPolicy().getId(), APPROVED,
        Timestamp.valueOf(currentTime));
    final Integer approvalBalance = (null == balance ? null : (balance - approvedHours));
    return approvalBalance;
  }


  private void saveTimeOffRequestDates(
      final TimeOffRequestCreateDto requestCreateDto, final TimeOffRequest timeOffRequest) {
    final List<TimeOffRequestDate> timeOffRequestDates =
        requestCreateDto.getTimeOffRequestDates(timeOffRequest);
    timeOffRequest.setTimeOffRequestDates(new HashSet<>(timeOffRequestDates));
    timeOffRequestDateService.saveAllTimeOffRequestDates(timeOffRequestDates);
  }

  public List<TimeOffRequestDto> findTimeOffRequests(
      final String id, final TimeOffApprovalStatus[] status) {
    final User user = userService.findById(id);

    final List<TimeOffRequestDto> timeOffRequestDtos;
    timeOffRequestDtos = getRequestsByUserAndStatus(user, status).stream()
        .map(timeOffRequestMapper::convertToTimeOffRequestDto)
        .collect(Collectors.toList());

    return timeOffRequestDtos;
  }

  public PageImpl<TimeOffRequestDto> findRequestsByApproverAndStatuses(
      final PageRequest pageRequest, final String[] statuses, final AuthUser authUser) {
    final Timestamp startDayTimestamp = DateUtil.getFirstDayOfCurrentYear();

    final Page<TimeOffRequest> timeOffRequests = this
        .getByApproverAndStatusFilteredByStartDay(
            authUser.getId(), statuses, startDayTimestamp, pageRequest);

    return (PageImpl<TimeOffRequestDto>) timeOffRequests
        .map(timeOffRequestMapper::convertToTimeOffRequestDto);
  }


  public enum SortFields {
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
}
