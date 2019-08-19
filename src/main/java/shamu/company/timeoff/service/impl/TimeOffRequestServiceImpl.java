package shamu.company.timeoff.service.impl;

import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.APPROVED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.DENIED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.NO_ACTION;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.VIEWED;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.timeoff.dto.BasicTimeOffRequestDto;
import shamu.company.timeoff.dto.MyTimeOffDto;
import shamu.company.timeoff.dto.TimeOffRequestDetailDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestComment;
import shamu.company.timeoff.entity.mapper.TimeOffRequestMapper;
import shamu.company.timeoff.pojo.UnimplementedRequestPojo;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestDateRepository;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.timeoff.service.TimeOffRequestEmailService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole.Role;
import shamu.company.user.repository.UserRepository;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffRequestServiceImpl implements TimeOffRequestService {

  private final TimeOffRequestRepository timeOffRequestRepository;

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  private final TimeOffRequestDateRepository timeOffRequestDateRepository;

  private final UserRepository userRepository;

  private final TimeOffRequestEmailService timeOffRequestEmailService;

  private final TimeOffRequestMapper timeOffRequestMapper;

  @Autowired
  public TimeOffRequestServiceImpl(
      final TimeOffRequestRepository timeOffRequestRepository,
      final TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      final UserRepository userRepository,
      final TimeOffRequestDateRepository timeOffRequestDateRepository,
      @Lazy final TimeOffRequestEmailService timeOffRequestEmailService,
      final TimeOffRequestMapper timeOffRequestMapper) {
    this.timeOffRequestRepository = timeOffRequestRepository;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.userRepository = userRepository;
    this.timeOffRequestDateRepository = timeOffRequestDateRepository;
    this.timeOffRequestEmailService = timeOffRequestEmailService;
    this.timeOffRequestMapper = timeOffRequestMapper;
  }

  @Override
  public Page<TimeOffRequest> getByApproverAndStatusFilteredByStartDay(
      final User approver, final Long[] statusIds,
      final Timestamp startDay, final PageRequest pageRequest) {
    return timeOffRequestRepository.findByApproversAndTimeOffApprovalStatusFilteredByStartDay(
        approver.getId(), statusIds, startDay, pageRequest);
  }

  @Override
  public Integer getPendingRequestsCount(final User approver) {
    final TimeOffRequestApprovalStatus[] statuses = new TimeOffRequestApprovalStatus[]{
        NO_ACTION, VIEWED
    };
    return timeOffRequestRepository.countByApproversContainingAndTimeOffApprovalStatusIsIn(
        approver, statuses);
  }

  @Override
  public TimeOffRequest getById(final Long timeOffRequestId) {
    return timeOffRequestRepository
        .findById(timeOffRequestId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("No time off request with id: " + timeOffRequestId));
  }

  @Override
  public TimeOffRequest save(final TimeOffRequest timeOffRequest) {
    return timeOffRequestRepository.save(timeOffRequest);
  }

  @Override
  public TimeOffRequest createTimeOffRequest(final TimeOffRequest request) {
    return timeOffRequestRepository.save(request);
  }

  @Override
  public List<TimeOffRequest> getRequestsByUserAndStatus(
      final User user, final TimeOffRequestApprovalStatus[] status) {
    final List<TimeOffRequestApprovalStatus> statusList = Arrays.asList(status);
    final List<String> statusNames = statusList.stream().map(Enum::name)
        .collect(Collectors.toList());

    if (user.getRole().name().equals(Role.NON_MANAGER.name())) {
      return timeOffRequestRepository.employeeFindTeamRequests(
          user.getManagerUser().getId(), statusNames);
    } else if (user.getRole().name().equals(Role.MANAGER.name())) {
      return timeOffRequestRepository.managerFindTeamRequests(
          user.getId(), user.getManagerUser().getId(), statusNames);
    } else {
      return timeOffRequestRepository.managerFindTeamRequests(user.getId(), null, statusNames);
    }
  }

  private MyTimeOffDto initMyTimeOff(
      final Long id, final Timestamp startDay, final Timestamp endDay,
      final Boolean filteredByEndDay,
      final Long[] statuses, final PageRequest request) {
    final MyTimeOffDto myTimeOffDto = new MyTimeOffDto();
    final Boolean policiesAdded = timeOffPolicyUserRepository.existsByUserId(id);
    myTimeOffDto.setPoliciesAdded(policiesAdded);

    if (policiesAdded) {
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
    }

    return myTimeOffDto;
  }

  @Override
  public TimeOffRequestDto getMyTimeOffApprovedRequestsByRequesterUserIdAfterNow(
      final Long id, final Timestamp startDay, final Long status) {
    final TimeOffRequest timeOffRequest = timeOffRequestRepository
        .findByRequesterUserIdFilteredByApprovedAndStartDay(id, startDay, status);
    return timeOffRequestMapper.convertToTimeOffRequestDto(timeOffRequest);
  }

  @Override
  public MyTimeOffDto getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
      final Long id, final Timestamp startDay, final Long[] statuses, final PageRequest request) {
    return initMyTimeOff(id, startDay, null, false, statuses, request);
  }

  @Override
  public MyTimeOffDto getMyTimeOffRequestsByRequesterUserIdFilteredByStartAndEndDay(
      final Long id, final Timestamp startDay, final Timestamp endDay,
      final Long[] statuses, final PageRequest request) {
    return initMyTimeOff(id, startDay, endDay, true, statuses, request);
  }

  @Override
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

  @Override
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

  @Override
  public TimeOffRequest updateTimeOffRequest(
      final TimeOffRequest timeOffRequest, final TimeOffRequestComment timeOffRequestComment) {

    TimeOffRequest original = getById(timeOffRequest.getId());
    final TimeOffRequestApprovalStatus originalStatus = original.getTimeOffApprovalStatus();
    final TimeOffRequestApprovalStatus status = timeOffRequest.getTimeOffApprovalStatus();

    if (status != originalStatus && (status == APPROVED || originalStatus == APPROVED)) {
      final TimeOffPolicyUser timeOffPolicyUser =
          timeOffPolicyUserRepository.findTimeOffPolicyUserByUserAndTimeOffPolicy(
              original.getRequesterUser(), original.getTimeOffPolicy());
      Integer balance = timeOffPolicyUser.getBalance();
      final Integer hours = original.getHours();
      if (status == APPROVED) {
        balance -= hours;
      } else {
        balance += hours;
      }
      timeOffPolicyUser.setBalance(balance);
      timeOffPolicyUserRepository.save(timeOffPolicyUser);
    }

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
  @Override
  public void deleteUnimplementedRequest(
      final Long requestId, final UnimplementedRequestPojo unimplementedRequestPojo) {
    final TimeOffRequest timeOffRequest =
        timeOffRequestRepository.findById(requestId).get();
    final TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
        timeOffRequest.getTimeOffApprovalStatus();
    final TimeOffPolicy timeOffPolicy = timeOffRequest.getTimeOffPolicy();
    if (timeOffRequestApprovalStatus.equals(APPROVED) && timeOffPolicy.getIsLimited()) {
      final TimeOffPolicyUser timeOffPolicyUser =
          timeOffPolicyUserRepository.findTimeOffPolicyUserByUserAndTimeOffPolicy(
              new User(unimplementedRequestPojo.getUserId()), timeOffPolicy);
      timeOffPolicyUser.setBalance(
          timeOffPolicyUser.getBalance() + unimplementedRequestPojo.getHours());
      timeOffPolicyUserRepository.save(timeOffPolicyUser);
    }
    timeOffRequestRepository.delete(requestId);
    timeOffRequestDateRepository.deleteByTimeOffRequestId(requestId);
  }


  @Override
  public TimeOffRequestDetailDto getTimeOffRequestDetail(final Long id, final Long userId) {
    final TimeOffRequest timeOffRequest = this.getById(id);
    final User requester = timeOffRequest.getRequesterUser();
    final TimeOffRequestDetailDto requestDetail = timeOffRequestMapper
        .convertToTimeOffRequestDetailDto(timeOffRequest);

    final Integer balance = timeOffPolicyUserRepository
        .findTimeOffPolicyUserByUserAndTimeOffPolicy(requester, timeOffRequest.getTimeOffPolicy())
        .getBalance();
    requestDetail.setBalance(balance);

    if (timeOffRequest.getTimeOffApprovalStatus() == NO_ACTION
        && userId.equals(requester.getManagerUser().getId())) {
      timeOffRequest.setTimeOffApprovalStatus(TimeOffRequestApprovalStatus.VIEWED);
      timeOffRequestRepository.save(timeOffRequest);
    }

    if (requester.getManagerUser() != null) {
      final List<BasicTimeOffRequestDto> timeOffRequests =
          this.getOtherRequestsBy(timeOffRequest).stream()
              .map(timeOffRequestMapper::convertToBasicTimeOffRequestDto)
              .collect(Collectors.toList());
      requestDetail.setOtherTimeOffRequests(timeOffRequests);
    }

    return requestDetail;
  }
}
