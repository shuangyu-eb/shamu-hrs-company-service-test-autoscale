package shamu.company.timeoff.service.impl;

import static shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.APPROVED;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole.Role;
import shamu.company.user.repository.UserRepository;
import shamu.company.utils.AwsUtil;

@Service
public class TimeOffRequestServiceImpl implements TimeOffRequestService {

  private final TimeOffRequestRepository timeOffRequestRepository;

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  private final UserRepository userRepository;

  private final ApplicationConfig applicationConfig;

  private final EmailService emailService;

  private final ITemplateEngine templateEngine;

  private final AwsUtil awsUtil;

  @Autowired
  public TimeOffRequestServiceImpl(TimeOffRequestRepository timeOffRequestRepository,
      TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      UserRepository userRepository,
      ApplicationConfig applicationConfig, EmailService emailService,
      ITemplateEngine templateEngine, AwsUtil awsUtil) {
    this.timeOffRequestRepository = timeOffRequestRepository;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.userRepository = userRepository;
    this.applicationConfig = applicationConfig;
    this.emailService = emailService;
    this.templateEngine = templateEngine;
    this.awsUtil = awsUtil;
  }

  @Override
  public List<TimeOffRequest> getByApproverAndStatus(User approver,
      TimeOffRequestApprovalStatus[] status) {
    return timeOffRequestRepository.findByApproverUserAndTimeOffApprovalStatusIn(approver, status);
  }

  @Override
  public List<TimeOffRequest> getByRequestersAndStatus(List<User> requsters,
      TimeOffRequestApprovalStatus status) {
    return timeOffRequestRepository
        .findByRequesterUserInAndTimeOffApprovalStatus(requsters, status);
  }

  @Override
  public Integer getCountByApproverAndStatusIsNoAction(User approver) {
    return timeOffRequestRepository.countByApproverUserAndTimeOffApprovalStatus(approver,
        TimeOffRequestApprovalStatus.NO_ACTION);
  }

  @Override
  public TimeOffRequest getById(Long timeOffRequestId) {
    return timeOffRequestRepository.findById(timeOffRequestId).orElseThrow(
        () -> new ResourceNotFoundException("No time off request with id: " + timeOffRequestId));
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
  public List<TimeOffRequest> getRequestsByUserAndStatus(User user,
      TimeOffRequestApprovalStatus[] status) {
    List<TimeOffRequestApprovalStatus> statusList = Arrays.asList(status);
    List<String> statusNames = statusList.stream().map(element -> element.name())
        .collect(Collectors.toList());

    if (user.getRole().name().equals(Role.NON_MANAGER.name())) {
      return timeOffRequestRepository
          .employeeFindTeamRequests(user.getManagerUser().getId(), statusNames);
    } else if (user.getRole().name().equals(Role.MANAGER.name())) {
      return timeOffRequestRepository
          .managerFindTeamRequests(user.getId(), user.getManagerUser().getId(),
              statusNames);
    } else {
      return timeOffRequestRepository
          .managerFindTeamRequests(user.getId(), null,
              statusNames);
    }
  }

  @Override
  public MyTimeOffDto getMyTimeOffRequestsByRequesterUserId(Long id) {
    MyTimeOffDto myTimeOffDto = new MyTimeOffDto();
    Boolean policiesAdded = timeOffPolicyUserRepository.existsByUserId(id);
    myTimeOffDto.setPoliciesAdded(policiesAdded);

    if (policiesAdded) {
      List<TimeOffRequest> timeOffRequests = timeOffRequestRepository.findByRequesterUserId(id);
      List<TimeOffRequestDto> timeOffRequestDtos = timeOffRequests.stream()
          .map(TimeOffRequestDto::new).collect(Collectors.toList());
      myTimeOffDto.setTimeOffRequestDtos(timeOffRequestDtos);
    }

    return myTimeOffDto;
  }

  @Override
  public List<TimeOffRequest> getTimeOffHistories(Long userId, Long startTime, Long endTime) {
    List<Long> timeOffRequestIds = timeOffRequestRepository
        .getTimeOffRequestHistoryIds(userId, startTime, endTime);
    if (timeOffRequestIds != null) {
      return timeOffRequestRepository.findAllById(timeOffRequestIds);
    }
    return null;
  }

  @Override
  public List<TimeOffRequest> getOtherTimeOffRequestsByManager(User manager) {
    List<User> requesters = userRepository.findAllByManagerUserId(manager.getId());
    requesters.add(manager);
    return this.getByRequestersAndStatus(requesters, APPROVED);
  }

  @Override
  public void sendTimeOffRequestEmail(TimeOffRequest timeOffRequest) {
    User approver = timeOffRequest.getApproverUser();
    User requester = timeOffRequest.getRequesterUser();
    TimeOffRequestApprovalStatus status = timeOffRequest.getTimeOffApprovalStatus();

    Map<String, Object> variables = this.getVariablesOfTimeOffRequestEmail(timeOffRequest);
    String template;
    Email email;
    if (status == APPROVED || status == TimeOffRequestApprovalStatus.DENIED) {
      String subject = "Time Off " + (status == APPROVED ? "Approved" : "Denied");
      template = "time_off_request_approve_deny.html";
      email = new Email(approver, requester, subject);

      variables.put("approverComment", timeOffRequest.getApproverComment());
      variables.put("approverId", approver.getId());
      variables.put("approverName", approver.getUserPersonalInformation().getName());
      variables.put("approverImageUrl", awsUtil.getFullFileUrl(approver.getImageUrl()));
    } else {
      email = new Email(requester, approver, "Time Off Request");
      template = "time_off_request_pending.html";

      long conflict = this.getConflictOfTimeOffRequest(timeOffRequest);
      Integer balance = timeOffPolicyUserRepository.getBalanceByUserId(requester.getId());
      variables.put("remain", balance - timeOffRequest.getHours());
      variables.put("conflict", conflict);
      variables.put("requesterId", requester.getId());
      variables.put("requesterName", requester.getUserPersonalInformation().getName());
    }

    String emailContent = templateEngine.process(template, new Context(Locale.ENGLISH, variables));
    email.setSendDate(new Timestamp(new Date().getTime()));
    email.setContent(emailContent);

    emailService.saveAndScheduleEmail(email);
  }

  private long getConflictOfTimeOffRequest(TimeOffRequest timeOffRequest) {
    LocalDate start = timeOffRequest.getStartDay().toLocalDateTime().toLocalDate();
    LocalDate end = timeOffRequest.getEndDay().toLocalDateTime().toLocalDate();

    User manager = timeOffRequest.getRequesterUser().getManagerUser();
    List<TimeOffRequest> timeOffRequests = this.getOtherTimeOffRequestsByManager(manager);

    return timeOffRequests.stream()
        .filter(tr -> (start.compareTo(tr.getEndDay().toLocalDateTime().toLocalDate()) <= 0
            && end.compareTo(tr.getStartDay().toLocalDateTime().toLocalDate()) >= 0)).count();
  }

  private Map<String, Object> getVariablesOfTimeOffRequestEmail(TimeOffRequest timeOffRequest) {

    Map<String, Object> variables = new HashMap<>();

    LocalDate start = timeOffRequest.getStartDay().toLocalDateTime().toLocalDate();
    LocalDate end = timeOffRequest.getEndDay().toLocalDateTime().toLocalDate();
    String startDay;
    String endDay;
    if (start.getYear() == end.getYear()) {
      startDay = DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH).format(start);
      if (start.getMonth() == end.getMonth()) {
        endDay = DateTimeFormatter.ofPattern("d, YYYY", Locale.ENGLISH).format(start);
      } else {
        endDay = DateTimeFormatter.ofPattern("MMMM d, YYYY", Locale.ENGLISH).format(start);
      }
    } else {
      startDay = DateTimeFormatter.ofPattern("MMMM d, YYYY", Locale.ENGLISH).format(start);
      endDay = DateTimeFormatter.ofPattern("MMMM d, YYYY", Locale.ENGLISH).format(start);
    }

    variables.put("frontEndAddress", applicationConfig.getFrontEndAddress());
    variables.put("timeRange", startDay.concat(" - ").concat(endDay));
    variables.put("status", timeOffRequest.getTimeOffApprovalStatus().name());
    variables.put("type", timeOffRequest.getTimeOffPolicy().getName());
    variables.put("hours", timeOffRequest.getHours());
    variables.put("comment", timeOffRequest.getComment());
    variables.put("requesterImageUrl",
        awsUtil.getFullFileUrl(timeOffRequest.getRequesterUser().getImageUrl()));
    variables.put("helpUrl", applicationConfig.getHelpUrl());

    return variables;
  }
}
