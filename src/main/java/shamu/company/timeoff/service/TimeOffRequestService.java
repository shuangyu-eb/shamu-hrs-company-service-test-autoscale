package shamu.company.timeoff.service;

import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.AWAITING_REVIEW;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.DENIED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus.NO_ACTION;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
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
import shamu.company.server.AuthUser;
import shamu.company.timeoff.dto.BasicTimeOffRequestDto;
import shamu.company.timeoff.dto.MyTimeOffDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffRequestDetailDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestUpdateDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestComment;
import shamu.company.timeoff.entity.mapper.TimeOffRequestMapper;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestApprovalStatusRepository;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;
import shamu.company.utils.DateUtil;
import shamu.company.utils.UuidUtil;

@Service
public class TimeOffRequestService {

  private final TimeOffRequestRepository timeOffRequestRepository;

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  private final UserRepository userRepository;

  private final TimeOffRequestEmailService timeOffRequestEmailService;

  private final TimeOffRequestMapper timeOffRequestMapper;

  private final TimeOffPolicyService timeOffPolicyService;

  private final TimeOffDetailService timeOffDetailService;

  private final Auth0Util auth0Util;

  private final UserService userService;

  private final TimeOffRequestApprovalStatusRepository requestApprovalStatusRepository;


  @Autowired
  public TimeOffRequestService(
      final TimeOffRequestRepository timeOffRequestRepository,
      final TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      final UserRepository userRepository,
      @Lazy final TimeOffRequestEmailService timeOffRequestEmailService,
      final TimeOffRequestMapper timeOffRequestMapper,
      final TimeOffPolicyService timeOffPolicyService,
      final TimeOffDetailService timeOffDetailService,
      final Auth0Util auth0Util,
      final UserService userService,
      final TimeOffRequestApprovalStatusRepository approvalStatusRepository) {
    this.timeOffRequestRepository = timeOffRequestRepository;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.userRepository = userRepository;
    this.timeOffRequestEmailService = timeOffRequestEmailService;
    this.timeOffRequestMapper = timeOffRequestMapper;
    this.timeOffPolicyService = timeOffPolicyService;
    this.timeOffDetailService = timeOffDetailService;
    this.auth0Util = auth0Util;
    this.userService = userService;
    this.requestApprovalStatusRepository = approvalStatusRepository;
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

    final User.Role userRole = auth0Util
        .getUserRole(user.getId());

    final List<TimeOffRequest> result;
    final List<TimeOffRequest> selfPendingRequests = timeOffRequestRepository
            .employeeFindSelfPendingRequests(user.getId(),
                AWAITING_REVIEW.name());
    if (user.getManagerUser() == null) {
      result = timeOffRequestRepository.adminFindTeamRequests(user.getId(), statusNames);
    } else if (userRole == User.Role.MANAGER || userRole == User.Role.ADMIN) {
      result =  timeOffRequestRepository.managerFindTeamRequests(
          user.getId(), user.getManagerUser().getId(), statusNames);
    } else {
      result =  timeOffRequestRepository.employeeFindTeamRequests(
          user.getManagerUser().getId(), statusNames);
    }
    result.addAll(selfPendingRequests);
    return result;
  }

  private MyTimeOffDto initMyTimeOff(
      final String id, final Timestamp startDay, final Timestamp endDay,
      final Boolean filteredByEndDay,
      final String[] statuses, final PageRequest request) {
    final MyTimeOffDto myTimeOffDto = new MyTimeOffDto();
    final Boolean policiesAdded = timeOffPolicyUserRepository.existsByUserId(id);
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

  public TimeOffRequestDto getRecentApprovedRequestByRequesterUserId(
      final String id, final Timestamp startDay, final String statusName) {
    final TimeOffRequest timeOffRequest = timeOffRequestRepository
        .findRecentApprovedRequestByRequesterUserId(id, startDay, statusName);
    return timeOffRequestMapper.convertToTimeOffRequestDto(timeOffRequest);
  }

  public MyTimeOffDto getReviewedRequests(final String id,
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
    return initMyTimeOff(id, startDay, null, false, statuses, request);
  }

  private MyTimeOffDto getMyTimeOffRequestsByRequesterUserIdFilteredByStartAndEndDay(
      final String id, final Timestamp startDay, final Timestamp endDay,
      final String[] statuses, final PageRequest request) {
    return initMyTimeOff(id, startDay, endDay, true, statuses, request);
  }

  public MyTimeOffDto getMyTimeOffRequestsByRequesterUserId(
      final String id, final Timestamp startDay) {
    final MyTimeOffDto myTimeOffDto = new MyTimeOffDto();
    final Boolean policiesAdded = timeOffPolicyUserRepository.existsByUserId(id);
    myTimeOffDto.setPoliciesAdded(policiesAdded);

    final List<TimeOffRequest> timeOffRequests = timeOffRequestRepository
        .findByRequesterUserIdFilteredByStartDayWithoutPaging(id, startDay);

    final List<TimeOffRequestDto> timeOffRequestDtos =
        timeOffRequests.stream()
            .map(timeOffRequestMapper::convertToTimeOffRequestDto).collect(Collectors.toList());

    myTimeOffDto.setTimeOffRequests(
        new PageImpl<>(timeOffRequestDtos, PageRequest.of(0, 0), timeOffRequests.size()));

    return myTimeOffDto;
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
    final List<User> requesters = userRepository.findAllByManagerUserId(requester.getId());

    if (manager != null) {
      requesters.addAll(userRepository.findAllByManagerUserId(manager.getId()));
      requesters.add(manager);
    } else {
      requesters.add(requester);
    }

    List<byte[]> requesterIds = requesters.stream().map(User::getId)
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
      timeOffRequest.setApprovedDate(Timestamp.from(Instant.now()));
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

    TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
        requestApprovalStatusRepository.findByName(status.name());
    original.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);
    original.setApproverUser(timeOffRequest.getApproverUser());
    original.setApprovedDate(Timestamp.from(Instant.now()));

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


  public TimeOffRequestDetailDto getTimeOffRequestDetail(final String id, final String userId) {
    final TimeOffRequest timeOffRequest = getById(id);
    final User requester = timeOffRequest.getRequesterUser();
    final TimeOffRequestDetailDto requestDetail = timeOffRequestMapper
        .convertToTimeOffRequestDetailDto(timeOffRequest);

    final List<BasicTimeOffRequestDto> timeOffRequests =
        getOtherRequestsBy(timeOffRequest).stream()
            .map(timeOffRequestMapper::convertToBasicTimeOffRequestDto)
            .collect(Collectors.toList());
    requestDetail.setOtherTimeOffRequests(timeOffRequests);

    return requestDetail;
  }

  public TimeOffRequest saveTimeOffRequest(
      final TimeOffRequest timeOffRequest, final String policyId,
      final TimeOffApprovalStatus status) {
    final TimeOffPolicy policy = timeOffPolicyService.getTimeOffPolicyById(policyId);
    timeOffRequest.setTimeOffPolicy(policy);

    TimeOffRequestApprovalStatus timeOffRequestApprovalStatus = requestApprovalStatusRepository
        .findByName(status.name());
    timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);
    final TimeOffPolicyUser timeOffPolicyUser = timeOffPolicyUserRepository
        .findTimeOffPolicyUserByUserAndTimeOffPolicy(timeOffRequest.getRequesterUser(), policy);

    final LocalDateTime currentTime = LocalDateTime.now();
    final TimeOffBreakdownDto timeOffBreakdownDto = timeOffDetailService
        .getTimeOffBreakdown(timeOffPolicyUser.getId(), currentTime.toLocalDate());
    final Integer balance = timeOffBreakdownDto.getBalance();
    final Integer approvedHours = timeOffPolicyService.getTimeOffRequestHoursFromStatus(
            timeOffPolicyUser.getUser().getId(),
            timeOffPolicyUser.getTimeOffPolicy().getId(), APPROVED,
            Timestamp.valueOf(currentTime));

    final Integer approvalBalance = (null == balance ? null : (balance - approvedHours));

    timeOffRequest.setBalance(approvalBalance);

    return createTimeOffRequest(timeOffRequest);
  }

  public List<TimeOffRequestDto> getTimeOffRequest(
      final String id, final TimeOffApprovalStatus[] status) {
    final User user = userService.findUserById(id);

    final List<TimeOffRequestDto> timeOffRequestDtos;
    timeOffRequestDtos = getRequestsByUserAndStatus(user, status).stream()
        .map(timeOffRequestMapper::convertToTimeOffRequestDto)
        .collect(Collectors.toList());

    return timeOffRequestDtos;
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
