package shamu.company.timeoff.service.impl;

import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.APPROVED;
import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.DENIED;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.ApplicationConfig;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.email.Email;
import shamu.company.email.EmailService;
import shamu.company.timeoff.dto.MyTimeOffDto;
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
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole.Role;
import shamu.company.user.repository.UserRepository;
import shamu.company.utils.AwsUtil;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffRequestServiceImpl implements TimeOffRequestService {

  private final TimeOffRequestRepository timeOffRequestRepository;

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  private final TimeOffRequestDateRepository timeOffRequestDateRepository;

  private final UserRepository userRepository;

  private final ApplicationConfig applicationConfig;

  private final EmailService emailService;

  private final ITemplateEngine templateEngine;

  private final AwsUtil awsUtil;

  @Autowired
  public TimeOffRequestServiceImpl(
      TimeOffRequestRepository timeOffRequestRepository,
      TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      UserRepository userRepository,
      ApplicationConfig applicationConfig,
      EmailService emailService,
      ITemplateEngine templateEngine,
      AwsUtil awsUtil,
      TimeOffRequestDateRepository timeOffRequestDateRepository) {
    this.timeOffRequestRepository = timeOffRequestRepository;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.userRepository = userRepository;
    this.applicationConfig = applicationConfig;
    this.emailService = emailService;
    this.templateEngine = templateEngine;
    this.awsUtil = awsUtil;
    this.timeOffRequestDateRepository = timeOffRequestDateRepository;
  }

  @Override
  public List<TimeOffRequest> getByApproverAndStatus(
      User approver, TimeOffRequestApprovalStatus[] status, Timestamp startDay, Timestamp endDay) {
    return timeOffRequestRepository.findByApproversAndTimeOffApprovalStatusFilteredByStartAndEndDay(
        approver.getId(), status, startDay, endDay);
  }

  @Override
  public Integer getCountByApproverAndStatusIsNoAction(User approver) {
    return timeOffRequestRepository.countByApproverUserAndTimeOffApprovalStatus(
        approver, TimeOffRequestApprovalStatus.NO_ACTION);
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

  @Override
  public MyTimeOffDto getMyTimeOffRequestsByRequesterUserId(
      Long id, Timestamp startDay, Timestamp endDay) {
    MyTimeOffDto myTimeOffDto = new MyTimeOffDto();
    Boolean policiesAdded = timeOffPolicyUserRepository.existsByUserId(id);
    myTimeOffDto.setPoliciesAdded(policiesAdded);

    if (policiesAdded) {
      List<TimeOffRequest> timeOffRequests =
          timeOffRequestRepository.findByRequesterUserIdFilteredByStartAndEndDay(
              id, startDay, endDay);
      List<TimeOffRequestDto> timeOffRequestDtos =
          timeOffRequests.stream().map(TimeOffRequestDto::new).collect(Collectors.toList());
      myTimeOffDto.setTimeOffRequests(timeOffRequestDtos);
    }

    return myTimeOffDto;
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
    return timeOffRequestRepository.findByRequesterUserInAndTimeOffApprovalStatus(
        requesters, APPROVED, start, end);
  }

  @Override
  public void sendTimeOffRequestEmail(TimeOffRequest timeOffRequest) {
    User requester = timeOffRequest.getRequesterUser();
    TimeOffRequestApprovalStatus status = timeOffRequest.getTimeOffApprovalStatus();

    Map<String, Object> variables = getVariablesOfTimeOffRequestEmail(timeOffRequest);
    String template;
    Email email;

    if (status == APPROVED || status == TimeOffRequestApprovalStatus.DENIED) {
      User approver = timeOffRequest.getApproverUser();

      boolean isApproved = status == APPROVED;
      String subject = "Time Off " + (isApproved ? "Approved" : "Denied");
      template = "time_off_request_" + (isApproved ? "approve" : "deny") + ".html";
      email = new Email(approver, requester, subject);

      variables.put("approverComments", timeOffRequest.getApproverComments());
      variables.put("approverId", approver.getId());
      variables.put("approverName", approver.getUserPersonalInformation().getName());
      variables.put("approverImageUrl", approver.getImageUrl());

      if (isApproved) {
        long conflict = getConflictOfTimeOffRequest(timeOffRequest);
        Integer balance = timeOffPolicyUserRepository.getBalanceByUserId(requester.getId());
        variables.put("remain", balance);
        variables.put("conflict", conflict);
      }

      sendEmail(variables, template, email);

      List<User> manager =
          timeOffRequest.getApprovers().stream()
              .filter(user -> !user.getId().equals(approver.getId()))
              .collect(Collectors.toList());
      if (!manager.isEmpty()) {
        String subject2 = subject + "by " + approver.getUserPersonalInformation().getName();
        variables.put("toManager", true);
        manager.forEach(
            user -> {
              Email managerEmail = new Email(approver, user, subject2);
              sendEmail(variables, template, managerEmail);
            });
      }
      return;
    }
    User approver = (User) timeOffRequest.getApprovers().toArray()[0];
    email = new Email(requester, approver, "Time Off Request");
    template = "time_off_request_pending.html";

    long conflict = getConflictOfTimeOffRequest(timeOffRequest);
    Integer balance = timeOffPolicyUserRepository.getBalanceByUserId(requester.getId());
    variables.put("remain", balance - timeOffRequest.getHours());
    variables.put("conflict", conflict);
    variables.put("requesterId", requester.getId());
    variables.put("requesterName", requester.getUserPersonalInformation().getName());

    sendEmail(variables, template, email);
  }

  private void sendEmail(Map<String, Object> variables, String template, Email email) {
    String emailContent = templateEngine.process(template, new Context(Locale.ENGLISH, variables));
    email.setSendDate(new Timestamp(new Date().getTime()));
    email.setContent(emailContent);

    emailService.saveAndScheduleEmail(email);
  }

  @Override
  public TimeOffRequest updateTimeOffRequest(
      TimeOffRequest timeOffRequest, TimeOffRequestComment timeOffRequestComment) {
    TimeOffRequest original = getById(timeOffRequest.getId());

    TimeOffRequestApprovalStatus status = timeOffRequest.getTimeOffApprovalStatus();
    original.setTimeOffApprovalStatus(status);
    original.setApproverUser(timeOffRequest.getApproverUser());
    original.setApprovedDate(Timestamp.from(Instant.now()));

    TimeOffRequestApprovalStatus originalStatus = original.getTimeOffApprovalStatus();
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
    if (timeOffRequestComment != null) {
      original.setComment(timeOffRequestComment);
    }

    original = timeOffRequestRepository.save(original);

    if (status == APPROVED || status == DENIED) {
      sendTimeOffRequestEmail(original);
    }

    return original;
  }

  @Override
  public List<TimeOffRequest> getTimeOffRequestsByTimeOffPolicyId(Long id) {
    return timeOffRequestRepository.findByTimeOffPolicyId(id);
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

  private long getConflictOfTimeOffRequest(TimeOffRequest timeOffRequest) {
    LocalDate start = timeOffRequest.getStartDay().toLocalDateTime().toLocalDate();
    LocalDate end = timeOffRequest.getEndDay().toLocalDateTime().toLocalDate();

    List<TimeOffRequest> timeOffRequests = getOtherRequestsBy(timeOffRequest);

    return timeOffRequests.stream()
        .filter(
            tr ->
                (start.compareTo(tr.getEndDay().toLocalDateTime().toLocalDate()) <= 0
                    && end.compareTo(tr.getStartDay().toLocalDateTime().toLocalDate()) >= 0))
        .count();
  }

  private Map<String, Object> getVariablesOfTimeOffRequestEmail(TimeOffRequest timeOffRequest) {

    Map<String, Object> variables = new HashMap<>();

    LocalDate start = timeOffRequest.getStartDay().toLocalDateTime().toLocalDate();
    LocalDate end = timeOffRequest.getEndDay().toLocalDateTime().toLocalDate();
    String startDay;
    String endDay;
    if (start.getYear() == end.getYear()) {
      startDay = DateUtil.formatDateTo(start, DateUtil.FULL_MONTH_DAY);
      if (start.getMonth() == end.getMonth()) {
        endDay = DateUtil.formatDateTo(end, "d, YYYY");
      } else {
        endDay = DateUtil.formatDateTo(end, DateUtil.FULL_MONTH_DAY_YEAR);
      }
    } else {
      startDay = DateUtil.formatDateTo(start, DateUtil.FULL_MONTH_DAY_YEAR);
      endDay = DateUtil.formatDateTo(end, DateUtil.FULL_MONTH_DAY_YEAR);
    }

    variables.put("frontEndAddress", applicationConfig.getFrontEndAddress());
    variables.put("timeRange", startDay.concat(" - ").concat(endDay));
    variables.put("status", timeOffRequest.getTimeOffApprovalStatus().name());
    variables.put("type", timeOffRequest.getTimeOffPolicy().getName());
    variables.put("hours", timeOffRequest.getHours());
    variables.put("comment", timeOffRequest.getRequsterComment());
    variables.put("requesterImageUrl", timeOffRequest.getRequesterUser().getImageUrl());
    variables.put("helpUrl", applicationConfig.getHelpUrl());
    variables.put("pathPrefix", awsUtil.getAwsPath());

    return variables;
  }
}
