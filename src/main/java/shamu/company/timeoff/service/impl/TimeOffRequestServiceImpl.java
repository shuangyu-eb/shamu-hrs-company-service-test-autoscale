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
import org.springframework.stereotype.Service;
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

  @Autowired
  public TimeOffRequestServiceImpl(
      TimeOffRequestRepository timeOffRequestRepository,
      TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      UserRepository userRepository,
      TimeOffRequestDateRepository timeOffRequestDateRepository,
      @Lazy TimeOffRequestEmailService timeOffRequestEmailService) {
    this.timeOffRequestRepository = timeOffRequestRepository;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.userRepository = userRepository;
    this.timeOffRequestDateRepository = timeOffRequestDateRepository;
    this.timeOffRequestEmailService = timeOffRequestEmailService;
  }

  @Override
  public List<TimeOffRequest> getByApproverAndStatusFilteredByStartDay(
      User approver, TimeOffRequestApprovalStatus[] status, Timestamp startDay) {
    List<TimeOffRequestApprovalStatus> statusList = Arrays.asList(status);
    List<String> statusNames = statusList.stream().map(Enum::name).collect(Collectors.toList());
    return timeOffRequestRepository.findByApproversAndTimeOffApprovalStatusFilteredByStartDay(
        approver.getId(), statusNames, startDay);
  }

  @Override
  public Integer getPendingRequestsCount(User approver) {
    TimeOffRequestApprovalStatus[] statuses = new TimeOffRequestApprovalStatus[]{
        NO_ACTION, VIEWED
    };
    return timeOffRequestRepository.countByApproversContainingAndTimeOffApprovalStatusIsIn(
        approver, statuses);
  }

  @Override
  public TimeOffRequest getById(Long timeOffRequestId) {
    return timeOffRequestRepository
        .findById(timeOffRequestId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("No time off request with id: " + timeOffRequestId));
  }

  @Override
  public TimeOffRequest save(TimeOffRequest timeOffRequest) {
    return timeOffRequestRepository.save(timeOffRequest);
  }

  @Override
  public TimeOffRequest createTimeOffRequest(TimeOffRequest request) {
    return timeOffRequestRepository.save(request);
  }

  @Override
  public List<TimeOffRequest> getRequestsByUserAndStatus(
      User user, TimeOffRequestApprovalStatus[] status) {
    List<TimeOffRequestApprovalStatus> statusList = Arrays.asList(status);
    List<String> statusNames = statusList.stream().map(Enum::name).collect(Collectors.toList());

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
      Long id, Timestamp startDay, Timestamp endDay, Boolean filteredByEndDay) {
    MyTimeOffDto myTimeOffDto = new MyTimeOffDto();
    Boolean policiesAdded = timeOffPolicyUserRepository.existsByUserId(id);
    myTimeOffDto.setPoliciesAdded(policiesAdded);

    if (policiesAdded) {
      List<TimeOffRequest> timeOffRequests;
      if (filteredByEndDay) {
        timeOffRequests =
            timeOffRequestRepository.findByRequesterUserIdFilteredByStartAndEndDay(
                id, startDay, endDay);
      } else {
        timeOffRequests =
            timeOffRequestRepository.findByRequesterUserIdFilteredByStartDay(id, startDay);
      }
      List<TimeOffRequestDto> timeOffRequestDtos =
          timeOffRequests.stream().map(TimeOffRequestDto::new).collect(Collectors.toList());
      myTimeOffDto.setTimeOffRequests(timeOffRequestDtos);
    }

    return myTimeOffDto;
  }

  @Override
  public MyTimeOffDto getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
      Long id, Timestamp startDay) {
    return initMyTimeOff(id, startDay, null, false);
  }

  @Override
  public MyTimeOffDto getMyTimeOffRequestsByRequesterUserIdFilteredByStartAndEndDay(
      Long id, Timestamp startDay, Timestamp endDay) {
    return initMyTimeOff(id, startDay, endDay, true);
  }

  @Override
  public List<TimeOffRequest> getOtherRequestsBy(TimeOffRequest timeOffRequest) {
    Timestamp start = DateUtil.getFirstDayOfCurrentMonth();
    Timestamp end;
    User requester = timeOffRequest.getRequesterUser();
    Timestamp startDay = timeOffRequest.getStartDay();
    if (start.before(startDay)) {
      end = DateUtil.getDayOfNextYear();
    } else {
      start = DateUtil.getFirstDayOfMonth(startDay);
      end = DateUtil.getDayOfNextYear(start);
    }

    User manager = requester.getManagerUser();
    List<User> requesters = userRepository.findAllByManagerUserId(requester.getId());

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
      TimeOffRequest timeOffRequest, TimeOffRequestComment timeOffRequestComment) {

    TimeOffRequest original = getById(timeOffRequest.getId());
    TimeOffRequestApprovalStatus originalStatus = original.getTimeOffApprovalStatus();
    TimeOffRequestApprovalStatus status = timeOffRequest.getTimeOffApprovalStatus();

    if (status != originalStatus && (status == APPROVED || originalStatus == APPROVED)) {
      TimeOffPolicyUser timeOffPolicyUser =
          timeOffPolicyUserRepository.findTimeOffPolicyUserByUserAndTimeOffPolicy(
              original.getRequesterUser(), original.getTimeOffPolicy());
      Integer balance = timeOffPolicyUser.getBalance();
      Integer hours = original.getHours();
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

  @Override
  public void deleteUnimplementedRequest(
      Long requestId, UnimplementedRequestPojo unimplementedRequestPojo) {
    timeOffRequestRepository.delete(requestId);
    if (unimplementedRequestPojo.getStatus() == APPROVED) {
      TimeOffPolicy timeOffPolicy =
          timeOffRequestRepository.getOne(unimplementedRequestPojo.getUserId()).getTimeOffPolicy();
      TimeOffPolicyUser timeOffPolicyUser =
          timeOffPolicyUserRepository.findTimeOffPolicyUserByUserAndTimeOffPolicy(
              new User(unimplementedRequestPojo.getUserId()), timeOffPolicy);
      timeOffPolicyUser.setBalance(
          timeOffPolicyUser.getBalance() + unimplementedRequestPojo.getHours());
      timeOffPolicyUserRepository.save(timeOffPolicyUser);
    }
    timeOffRequestDateRepository.deleteByTimeOffRequestId(requestId);
  }


  @Override
  public TimeOffRequestDetailDto getTimeOffRequestDetail(Long id, Long userId) {
    TimeOffRequest timeOffRequest = this.getById(id);
    User requester = timeOffRequest.getRequesterUser();
    TimeOffRequestDetailDto requestDetail = new TimeOffRequestDetailDto(timeOffRequest);

    Integer balance = timeOffPolicyUserRepository
        .findTimeOffPolicyUserByUserAndTimeOffPolicy(requester, timeOffRequest.getTimeOffPolicy())
        .getBalance();
    requestDetail.setBalance(balance);

    if (timeOffRequest.getTimeOffApprovalStatus() == NO_ACTION
        && userId.equals(requester.getManagerUser().getId())) {
      timeOffRequest.setTimeOffApprovalStatus(TimeOffRequestApprovalStatus.VIEWED);
      timeOffRequestRepository.save(timeOffRequest);
    }

    if (requester.getManagerUser() != null) {
      List<BasicTimeOffRequestDto> timeOffRequests =
          this.getOtherRequestsBy(timeOffRequest).stream()
              .map(BasicTimeOffRequestDto::new).collect(Collectors.toList());
      requestDetail.setOtherTimeOffRequests(timeOffRequests);
    }

    return requestDetail;
  }
}
