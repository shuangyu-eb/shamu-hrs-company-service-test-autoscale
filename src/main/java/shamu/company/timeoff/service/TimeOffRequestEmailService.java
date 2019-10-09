package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.ApplicationConfig;
import shamu.company.email.Email;
import shamu.company.email.EmailService;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.user.entity.User;
import shamu.company.utils.AwsUtil;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffRequestEmailService {

  private final EmailService emailService;


  private final AwsUtil awsUtil;

  private final ITemplateEngine templateEngine;

  private final ApplicationConfig applicationConfig;

  private final TimeOffRequestService timeOffRequestService;

  @Autowired
  public TimeOffRequestEmailService(final EmailService emailService,
      final AwsUtil awsUtil,
      final TemplateEngine templateEngine, final ApplicationConfig applicationConfig,
      @Lazy final TimeOffRequestService timeOffRequestService) {
    this.emailService = emailService;
    this.awsUtil = awsUtil;
    this.templateEngine = templateEngine;
    this.applicationConfig = applicationConfig;
    this.timeOffRequestService = timeOffRequestService;
  }

  public void sendEmail(TimeOffRequest timeOffRequest) {
    timeOffRequest = timeOffRequestService.findByRequestId(timeOffRequest.getId());
    final TimeOffRequestApprovalStatus status = timeOffRequest.getTimeOffApprovalStatus();
    if (status == TimeOffRequestApprovalStatus.DENIED) {
      sendDeniedEmail(timeOffRequest);
      return;
    }

    if (status == TimeOffRequestApprovalStatus.APPROVED) {
      sendApprovedEmail(timeOffRequest);
      return;
    }

    sendPendingEmail(timeOffRequest);
  }

  public void sendApprovedEmail(final TimeOffRequest timeOffRequest) {
    final Map<String, Object> variables = getVariablesOfTimeOffRequestEmail(timeOffRequest);

    final User approver = timeOffRequest.getApproverUser();

    setAproverMessage(timeOffRequest, variables, approver);

    final long conflict = getConflictOfTimeOffRequest(timeOffRequest);
    variables.put("conflict", conflict);

    if (timeOffRequest.getTimeOffPolicy().getIsLimited()) {
      final Integer balance = timeOffRequest.getBalance() - timeOffRequest.getHours();
      variables.put("remain", balance);
    }

    final String subject = "Time Off Approved";
    final String template = "time_off_request_approve.html";

    final User requester = timeOffRequest.getRequesterUser();

    processAndSendEmail(variables, template, new Email(approver, requester, subject));

    sendToManagers(timeOffRequest, variables, template, approver, subject);

  }

  private void setAproverMessage(final TimeOffRequest timeOffRequest,
      final Map<String, Object> variables,
      final User approver) {
    variables.put("approverComments", timeOffRequest.getApproverComments());
    variables.put("approverId", approver.getId());
    variables.put("approverName", approver.getUserPersonalInformation().getName());
    variables.put("approverImageUrl", approver.getImageUrl());
  }

  private void sendToManagers(final TimeOffRequest timeOffRequest,
      final Map<String, Object> variables,
      final String template, final User approver, final String subject) {
    final List<User> manager =
        timeOffRequest.getApprovers().stream()
            .filter(user -> !user.getId().equals(approver.getId()))
            .collect(Collectors.toList());
    if (!manager.isEmpty()) {
      final String subject2 = subject + " by " + approver.getUserPersonalInformation().getName();
      variables.put("toManager", true);
      manager.forEach(
          user -> {
            final Email managerEmail = new Email(approver, user, subject2);
            processAndSendEmail(variables, template, managerEmail);
          });
    }
  }


  public void sendDeniedEmail(final TimeOffRequest timeOffRequest) {
    final User requester = timeOffRequest.getRequesterUser();

    final Map<String, Object> variables = getVariablesOfTimeOffRequestEmail(timeOffRequest);

    final User approver = timeOffRequest.getApproverUser();

    final String subject = "Time Off Denied";
    final String template = "time_off_request_deny.html";
    final Email email = new Email(approver, requester, subject);

    setAproverMessage(timeOffRequest, variables, approver);

    processAndSendEmail(variables, template, email);

    sendToManagers(timeOffRequest, variables, template, approver, subject);
  }

  public void sendPendingEmail(final TimeOffRequest timeOffRequest) {
    final User requester = timeOffRequest.getRequesterUser();

    final Map<String, Object> variables = getVariablesOfTimeOffRequestEmail(timeOffRequest);

    final long conflict = getConflictOfTimeOffRequest(timeOffRequest);
    if (timeOffRequest.getTimeOffPolicy().getIsLimited()) {
      final Integer balance = timeOffRequest.getBalance();
      variables.put("remain", balance - timeOffRequest.getHours());
    }

    variables.put("conflict", conflict);

    final User approver = (User) timeOffRequest.getApprovers().toArray()[0];
    final Email email = new Email(requester, approver, "Time Off Request");

    processAndSendEmail(variables, "time_off_request_pending.html", email);
  }


  private void processAndSendEmail(
      final Map<String, Object> variables, final String template, final Email email) {
    final String emailContent = templateEngine
        .process(template, new Context(Locale.ENGLISH, variables));
    email.setSendDate(new Timestamp(new Date().getTime()));
    email.setContent(emailContent);

    emailService.saveAndScheduleEmail(email);
  }

  private Map<String, Object> getVariablesOfTimeOffRequestEmail(
      final TimeOffRequest timeOffRequest) {

    final Map<String, Object> variables = new HashMap<>();

    final LocalDate start = timeOffRequest.getStartDay().toLocalDateTime().toLocalDate();
    final LocalDate end = timeOffRequest.getEndDay().toLocalDateTime().toLocalDate();
    final String startDay;
    final String endDay;
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

    final User requester = timeOffRequest.getRequesterUser();

    variables.put("isLimited", timeOffRequest.getTimeOffPolicy().getIsLimited());

    variables.put("frontEndAddress", applicationConfig.getFrontEndAddress());
    variables.put("timeRange", startDay.concat(" - ").concat(endDay));
    variables.put("status", timeOffRequest.getTimeOffApprovalStatus().name());
    variables.put("type", timeOffRequest.getTimeOffPolicy().getName());
    variables.put("hours", timeOffRequest.getHours());
    variables.put("comment", timeOffRequest.getRequsterComment());
    variables.put("requesterImageUrl", requester.getImageUrl());
    variables.put("requesterId", requester.getId());
    variables.put("requesterName", requester.getUserPersonalInformation().getName());
    variables.put("helpUrl", applicationConfig.getHelpUrl());
    variables.put("pathPrefix", awsUtil.getAwsPath());

    return variables;
  }


  private long getConflictOfTimeOffRequest(final TimeOffRequest timeOffRequest) {
    final LocalDate start = timeOffRequest.getStartDay().toLocalDateTime().toLocalDate();
    final LocalDate end = timeOffRequest.getEndDay().toLocalDateTime().toLocalDate();

    final List<TimeOffRequest> timeOffRequests = timeOffRequestService
        .getOtherRequestsBy(timeOffRequest);

    return timeOffRequests.stream()
        .filter(
            tr ->
                (start.compareTo(tr.getEndDay().toLocalDateTime().toLocalDate()) <= 0
                    && end.compareTo(tr.getStartDay().toLocalDateTime().toLocalDate()) >= 0))
        .count();
  }
}
