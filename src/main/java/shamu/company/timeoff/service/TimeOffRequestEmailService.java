package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.ApplicationConfig;
import shamu.company.email.entity.Email;
import shamu.company.email.service.EmailService;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.pojo.TimeOffEmailCommentPojo;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.utils.AvatarUtil;

@Service
public class TimeOffRequestEmailService {

  private final EmailService emailService;

  private final AwsHelper awsHelper;

  private final ITemplateEngine templateEngine;

  private final ApplicationConfig applicationConfig;

  private final TimeOffRequestService timeOffRequestService;

  private final TimeOffDetailService timeOffDetailService;

  @Autowired
  public TimeOffRequestEmailService(
      final EmailService emailService,
      final AwsHelper awsHelper,
      final ITemplateEngine templateEngine,
      final ApplicationConfig applicationConfig,
      @Lazy final TimeOffRequestService timeOffRequestService,
      final TimeOffDetailService timeOffDetailService) {
    this.emailService = emailService;
    this.awsHelper = awsHelper;
    this.templateEngine = templateEngine;
    this.applicationConfig = applicationConfig;
    this.timeOffRequestService = timeOffRequestService;
    this.timeOffDetailService = timeOffDetailService;
  }

  public void sendEmail(TimeOffRequest timeOffRequest) {
    timeOffRequest = timeOffRequestService.findByRequestId(timeOffRequest.getId());
    final TimeOffApprovalStatus status = timeOffRequest.getApprovalStatus();
    if (status == TimeOffApprovalStatus.DENIED) {
      sendDeniedEmail(timeOffRequest);
      return;
    }

    if (status == TimeOffApprovalStatus.APPROVED) {
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

    processAndSendEmail(
        variables,
        template,
        new Email(
            applicationConfig.getSystemEmailAddress(),
            approver.getUserPersonalInformation().getName(),
            requester.getUserContactInformation().getEmailWork(),
            requester.getUserPersonalInformation().getName(),
            subject));

    sendToManagers(timeOffRequest, variables, template, approver, subject);
  }

  private void setAproverMessage(
      final TimeOffRequest timeOffRequest,
      final Map<String, Object> variables,
      final User approver) {

    final List<TimeOffEmailCommentPojo> commentPojos =
        timeOffRequest.getApproverComments().stream()
            .map(
                approveComment -> {
                  final TimeOffEmailCommentPojo timeOffEmailCommentPojo =
                      new TimeOffEmailCommentPojo();
                  timeOffEmailCommentPojo.setComment(approveComment.getComment());
                  final User approveUser = approveComment.getUser();
                  timeOffEmailCommentPojo.setImageUrl(approveUser.getImageUrl());
                  final String backgroundColor =
                      AvatarUtil.getAvatarBackground(
                          approveUser.getUserPersonalInformation().getFirstName());
                  final String avatarText =
                      AvatarUtil.getAvatarShortName(
                          approveUser.getUserPersonalInformation().getFirstName(),
                          approveUser.getUserPersonalInformation().getLastName());
                  timeOffEmailCommentPojo.setBackgroundColor(backgroundColor);
                  timeOffEmailCommentPojo.setAvatarText(avatarText);
                  return timeOffEmailCommentPojo;
                })
            .collect(Collectors.toList());

    variables.put("approverComments", commentPojos);

    variables.put("approverId", approver.getId());
    variables.put("approverName", approver.getUserPersonalInformation().getName());
    variables.put("approverImageUrl", approver.getImageUrl());
  }

  private void sendToManagers(
      final TimeOffRequest timeOffRequest,
      final Map<String, Object> variables,
      final String template,
      final User approver,
      final String subject) {

    final User requesterUser = timeOffRequest.getRequesterUser();
    final User managerUser = requesterUser.getManagerUser();
    final boolean approvedByManager =
        managerUser != null && managerUser.getId().equals(approver.getId());

    if (managerUser != null && !approvedByManager) {
      final String subject2 = subject + " by " + approver.getUserPersonalInformation().getName();
      variables.put("toManager", true);
      final Email managerEmail =
          new Email(
              applicationConfig.getSystemEmailAddress(),
              approver.getUserPersonalInformation().getName(),
              managerUser.getUserContactInformation().getEmailWork(),
              managerUser.getUserPersonalInformation().getName(),
              subject2);

      processAndSendEmail(variables, template, managerEmail);
    }
  }

  public void sendDeniedEmail(final TimeOffRequest timeOffRequest) {
    final User requester = timeOffRequest.getRequesterUser();

    final Map<String, Object> variables = getVariablesOfTimeOffRequestEmail(timeOffRequest);

    final User approver = timeOffRequest.getApproverUser();

    if (!requester.getId().equals(approver.getId())) {
      final String subject = "Time Off Denied";
      final String template = "time_off_request_deny.html";
      final Email email =
          new Email(
              applicationConfig.getSystemEmailAddress(),
              approver.getUserPersonalInformation().getName(),
              requester.getUserContactInformation().getEmailWork(),
              requester.getUserPersonalInformation().getName(),
              subject);

      setAproverMessage(timeOffRequest, variables, approver);

      processAndSendEmail(variables, template, email);

      sendToManagers(timeOffRequest, variables, template, approver, subject);
    }
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

    final User approver = timeOffRequest.getApproverUser();
    final Email email =
        new Email(
            applicationConfig.getSystemEmailAddress(),
            requester.getUserPersonalInformation().getName(),
            approver.getUserContactInformation().getEmailWork(),
            approver.getUserPersonalInformation().getName(),
            "Time Off Request");

    processAndSendEmail(variables, "time_off_request_pending.html", email);
  }

  private void processAndSendEmail(
      final Map<String, Object> variables, final String template, final Email email) {
    final String emailContent =
        templateEngine.process(template, new Context(Locale.ENGLISH, variables));
    email.setSendDate(new Timestamp(new Date().getTime()));
    email.setContent(emailContent);

    emailService.saveAndScheduleEmail(email);
  }

  private Map<String, Object> getVariablesOfTimeOffRequestEmail(
      final TimeOffRequest timeOffRequest) {

    final Map<String, Object> variables = new HashMap<>();

    final User requester = timeOffRequest.getRequesterUser();

    variables.put("isLimited", timeOffRequest.getTimeOffPolicy().getIsLimited());

    variables.put("frontEndAddress", applicationConfig.getFrontEndAddress());
    variables.put(
        "timeRange", timeOffDetailService.getTimeOffRequestDatesPreview(timeOffRequest.getId()));
    variables.put("status", timeOffRequest.getApprovalStatus().name());
    variables.put("type", timeOffRequest.getTimeOffPolicy().getName());
    variables.put("hours", timeOffRequest.getHours());
    variables.put("comment", timeOffRequest.getRequsterComment());

    if (StringUtils.isNotBlank(requester.getImageUrl())) {
      variables.put("requesterImageUrl", requester.getImageUrl());
    }

    final UserPersonalInformation requesterPersonalInformation =
        requester.getUserPersonalInformation();
    final String backgroundColor =
        AvatarUtil.getAvatarBackground(requesterPersonalInformation.getFirstName());
    final String shortName =
        AvatarUtil.getAvatarShortName(
            requesterPersonalInformation.getFirstName(),
            requesterPersonalInformation.getLastName());

    variables.put("backgroundColor", backgroundColor);
    variables.put("avatarText", shortName);

    variables.put("requesterId", requester.getId());
    variables.put("requesterName", requester.getUserPersonalInformation().getName());
    variables.put("helpUrl", applicationConfig.getHelpUrl());
    variables.put("pathPrefix", awsHelper.findAwsPath());

    return variables;
  }

  private long getConflictOfTimeOffRequest(final TimeOffRequest timeOffRequest) {
    final LocalDate start = timeOffRequest.getStartDay().toLocalDateTime().toLocalDate();
    final LocalDate end = timeOffRequest.getEndDay().toLocalDateTime().toLocalDate();

    final List<TimeOffRequest> timeOffRequests =
        timeOffRequestService.getOtherRequestsBy(timeOffRequest);

    return timeOffRequests.stream()
        .filter(
            tr ->
                (start.compareTo(tr.getEndDay().toLocalDateTime().toLocalDate()) <= 0
                    && end.compareTo(tr.getStartDay().toLocalDateTime().toLocalDate()) >= 0))
        .count();
  }
}
