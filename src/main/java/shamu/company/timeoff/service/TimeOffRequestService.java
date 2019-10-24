package shamu.company.timeoff.service;

import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.APPROVED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.DENIED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.NO_ACTION;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.VIEWED;

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
import shamu.company.timeoff.dto.UnimplementedRequestDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestComment;
import shamu.company.timeoff.entity.mapper.TimeOffRequestMapper;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestDateRepository;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffRequestService {

  private final TimeOffRequestRepository timeOffRequestRepository;

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  private final TimeOffRequestDateRepository timeOffRequestDateRepository;

  private final UserRepository userRepository;

  private final TimeOffRequestEmailService timeOffRequestEmailService;

  private final TimeOffRequestMapper timeOffRequestMapper;

  private final TimeOffPolicyService timeOffPolicyService;

  private final TimeOffDetailService timeOffDetailService;

  private final Auth0Util auth0Util;

  private final UserService userService;

  @Autowired
  public TimeOffRequestService(
      final TimeOffRequestRepository timeOffRequestRepository,
      final TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      final UserRepository userRepository,
      final TimeOffRequestDateRepository timeOffRequestDateRepository,
      @Lazy final TimeOffRequestEmailService timeOffRequestEmailService,
      final TimeOffRequestMapper timeOffRequestMapper,
      final TimeOffPolicyService timeOffPolicyService,
      final TimeOffDetailService timeOffDetailService,
      final Auth0Util auth0Util,
      final UserService userService) {
    this.timeOffRequestRepository = timeOffRequestRepository;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.userRepository = userRepository;
    this.timeOffRequestDateRepository = timeOffRequestDateRepository;
    this.timeOffRequestEmailService = timeOffRequestEmailService;
    this.timeOffRequestMapper = timeOffRequestMapper;
    this.timeOffPolicyService = timeOffPolicyService;
    this.timeOffDetailService = timeOffDetailService;
    this.auth0Util = auth0Util;
    this.userService = userService;
  }

  public TimeOffRequest findByRequestId(final Long id) {
    return timeOffRequestRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Request does not exist."));
  }

  public Page<TimeOffRequest> getByApproverAndStatusFilteredByStartDay(
      final Long id, final Long[] statusIds,
      final Timestamp startDay, final PageRequest pageRequest) {
    return timeOffRequestRepository.findByApproversAndTimeOffApprovalStatusFilteredByStartDay(
        id, statusIds, startDay, pageRequest);
  }

  public Integer getPendingRequestsCount(final User approver) {
    final TimeOffRequestApprovalStatus[] statuses = new TimeOffRequestApprovalStatus[]{
        NO_ACTION, VIEWED
    };
    return timeOffRequestRepository.countByApproversContainingAndTimeOffApprovalStatusIsIn(
        approver, statuses);
  }

  public TimeOffRequest getById(final Long timeOffRequestId) {
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
      final User user, final TimeOffRequestApprovalStatus[] status) {
    final List<TimeOffRequestApprovalStatus> statusList = Arrays.asList(status);
    final List<String> statusNames = statusList.stream().map(Enum::name)
        .collect(Collectors.toList());

    final User.Role userRole = auth0Util
        .getUserRole(user.getUserId());

    final List<TimeOffRequest> result;
    final List<TimeOffRequest> selfPendingRequests = timeOffRequestRepository
            .employeeFindSelfPendingRequests(user.getId());
    if (user.getManagerUser() == null) {
      result = timeOffRequestRepository.adminFindTeamRequests(user.getId(), statusNames);
    } else if (userRole == User.Role.MANAGER) {
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
      final Long id, final Timestamp startDay, final Timestamp endDay,
      final Boolean filteredByEndDay,
      final Long[] statuses, final PageRequest request) {
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
      final Long id, final Timestamp startDay, final Long statusId) {
    final TimeOffRequest timeOffRequest = timeOffRequestRepository
        .findRecentApprovedRequestByRequesterUserId(id, startDay, statusId);
    return timeOffRequestMapper.convertToTimeOffRequestDto(timeOffRequest);
  }

  public MyTimeOffDto getReviewedRequests(final Long id,
      final Long startDay,
      final Long endDay,
      final int page,
      final int size) {
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
      myTimeOffDto = getMyTimeOffRequestsByRequesterUserIdFilteredByStartAndEndDay(
          id, startDayTimestamp, new Timestamp(endDay), timeOffRequestStatuses, request);
    } else {
      myTimeOffDto = getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
          id, startDayTimestamp, timeOffRequestStatuses, request);
    }

    return myTimeOffDto;
  }

  public MyTimeOffDto getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
      final Long id, final Timestamp startDay, final Long[] statuses, final PageRequest request) {
    return initMyTimeOff(id, startDay, null, false, statuses, request);
  }

  private MyTimeOffDto getMyTimeOffRequestsByRequesterUserIdFilteredByStartAndEndDay(
      final Long id, final Timestamp startDay, final Timestamp endDay,
      final Long[] statuses, final PageRequest request) {
    return initMyTimeOff(id, startDay, endDay, true, statuses, request);
  }

  public MyTimeOffDto getMyTimeOffRequestsByRequesterUserId(
      final Long id, final Timestamp startDay) {
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
    List<TimeOffRequest> timeOffRequests =
        timeOffRequestRepository
            .findByRequesterUserInAndTimeOffApprovalStatus(requesters, APPROVED, start, end);

    if (timeOffRequest.getTimeOffApprovalStatus() == APPROVED) {
      timeOffRequests =
          timeOffRequests.stream()
              .filter(request -> !timeOffRequest.getId().equals(request.getId()))
              .collect(Collectors.toList());
    }

    return timeOffRequests;
  }

  public TimeOffRequestDto updateTimeOffRequestStatus(final Long id,
      final TimeOffRequestUpdateDto updateDto, final AuthUser user) {
    TimeOffRequest timeOffRequest = timeOffRequestMapper
        .createFromTimeOffRequestUpdateDto(updateDto);
    timeOffRequest.setId(id);

    final TimeOffRequestApprovalStatus status = updateDto.getStatus();
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
    final TimeOffRequestApprovalStatus status = timeOffRequest.getTimeOffApprovalStatus();

    original.setTimeOffApprovalStatus(status);
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
  public void deleteUnimplementedRequest(
      final Long requestId, final UnimplementedRequestDto unimplementedRequestDto) {
    final TimeOffRequest timeOffRequest = findByRequestId(requestId);
    final TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
        timeOffRequest.getTimeOffApprovalStatus();
    final TimeOffPolicy timeOffPolicy = timeOffRequest.getTimeOffPolicy();
    if (timeOffRequestApprovalStatus.equals(APPROVED) && timeOffPolicy.getIsLimited()) {
      final TimeOffPolicyUser timeOffPolicyUser =
          timeOffPolicyUserRepository.findTimeOffPolicyUserByUserAndTimeOffPolicy(
              new User(unimplementedRequestDto.getUserId()), timeOffPolicy);
      if (null != timeOffPolicyUser) {
        timeOffPolicyUser.setBalance(
            timeOffPolicyUser.getBalance() + unimplementedRequestDto.getHours());
        timeOffPolicyUserRepository.save(timeOffPolicyUser);
      }
    }
    timeOffRequestRepository.delete(requestId);
    timeOffRequestDateRepository.deleteByTimeOffRequestId(requestId);
  }


  public TimeOffRequestDetailDto getTimeOffRequestDetail(final Long id, final Long userId) {
    final TimeOffRequest timeOffRequest = getById(id);
    final User requester = timeOffRequest.getRequesterUser();
    final TimeOffRequestDetailDto requestDetail = timeOffRequestMapper
        .convertToTimeOffRequestDetailDto(timeOffRequest);

    if (timeOffRequest.getTimeOffApprovalStatus() == NO_ACTION
        && userId.equals(requester.getManagerUser().getId())) {
      timeOffRequest.setTimeOffApprovalStatus(TimeOffRequestApprovalStatus.VIEWED);
      timeOffRequestRepository.save(timeOffRequest);
    }

    final List<BasicTimeOffRequestDto> timeOffRequests =
        getOtherRequestsBy(timeOffRequest).stream()
            .map(timeOffRequestMapper::convertToBasicTimeOffRequestDto)
            .collect(Collectors.toList());
    requestDetail.setOtherTimeOffRequests(timeOffRequests);

    return requestDetail;
  }

  public TimeOffRequest saveTimeOffRequest(
      final TimeOffRequest timeOffRequest, final Long policyId,
      final TimeOffRequestApprovalStatus status) {
    final TimeOffPolicy policy = timeOffPolicyService.getTimeOffPolicyById(policyId);
    timeOffRequest.setTimeOffPolicy(policy);
    timeOffRequest.setTimeOffApprovalStatus(status);
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
      final Long id, final TimeOffRequestApprovalStatus[] status) {
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
