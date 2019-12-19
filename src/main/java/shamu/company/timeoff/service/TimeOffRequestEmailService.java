package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.ApplicationConfig;
import shamu.company.email.Email;
import shamu.company.email.EmailService;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.user.entity.User;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffRequestEmailService {

  private final EmailService emailService;


  private final AwsHelper awsHelper;

  private final ITemplateEngine templateEngine;

  private final ApplicationConfig applicationConfig;

  private final TimeOffRequestService timeOffRequestService;

  @Autowired
  public TimeOffRequestEmailService(final EmailService emailService,
      final AwsHelper awsHelper,
      final TemplateEngine templateEngine, final ApplicationConfig applicationConfig,
      @Lazy final TimeOffRequestService timeOffRequestService) {
    this.emailService = emailService;
    this.awsHelper = awsHelper;
    this.templateEngine = templateEngine;
    this.applicationConfig = applicationConfig;
    this.timeOffRequestService = timeOffRequestService;
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

    processAndSendEmail(variables, template, new Email(
            applicationConfig.getSystemEmailAddress(),
            approver.getUserPersonalInformation().getName(),
            requester.getUserContactInformation().getEmailWork(),
            requester.getUserPersonalInformation().getName(),
            subject));

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

    User requesterUser = timeOffRequest.getRequesterUser();
    User managerUser = requesterUser.getManagerUser();
    boolean approvedByManager = managerUser != null
        && managerUser.getId().equals(approver.getId());

    if (!approvedByManager) {
      final String subject2 = subject + " by " + approver.getUserPersonalInformation().getName();
      variables.put("toManager", true);
      final Email managerEmail = new Email(
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

    final String subject = "Time Off Denied";
    final String template = "time_off_request_deny.html";
    final Email email = new Email(
            applicationConfig.getSystemEmailAddress(),
            approver.getUserPersonalInformation().getName(),
            requester.getUserContactInformation().getEmailWork(),
            requester.getUserPersonalInformation().getName(),
            subject);

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

    final User approver = timeOffRequest.getApproverUser();
    final Email email = new Email(
            applicationConfig.getSystemEmailAddress(),
            requester.getUserPersonalInformation().getName(),
            approver.getUserContactInformation().getEmailWork(),
            approver.getUserPersonalInformation().getName(),
            "Time Off Request");

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

    final User requester = timeOffRequest.getRequesterUser();

    variables.put("isLimited", timeOffRequest.getTimeOffPolicy().getIsLimited());

    variables.put("frontEndAddress", applicationConfig.getFrontEndAddress());
    variables.put("timeRange", getTimeOffRange(timeOffRequest));
    variables.put("status", timeOffRequest.getApprovalStatus().name());
    variables.put("type", timeOffRequest.getTimeOffPolicy().getName());
    variables.put("hours", timeOffRequest.getHours());
    variables.put("comment", timeOffRequest.getRequsterComment());
    variables.put("requesterImageUrl", requester.getImageUrl());
    variables.put("requesterId", requester.getId());
    variables.put("requesterName", requester.getUserPersonalInformation().getName());
    variables.put("helpUrl", applicationConfig.getHelpUrl());
    variables.put("pathPrefix", awsHelper.findAwsPath());

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


  public String getTimeOffRange(final TimeOffRequest timeOffRequest) {
    final List<TimeOffDateRange> ranges = splitDates(timeOffRequest);
    final List<String> results = new ArrayList<>();

    for (int index = 0; index < ranges.size(); index++) {
      final TimeOffDateRange range = ranges.get(index);
      final LocalDate start = range.getStart();
      final LocalDate end = range.getEnd();
      if (isSameDate(range.getStart(), range.getEnd())) {
        results.add(handleSameDate(start, ranges, index));
        continue;
      }
      if (isSameMonth(start, end)) {
        results.add(handleSameMonth(ranges, index));
        continue;
      }
      if (isSameYear(start, end)) {
        results.add(handleSameYear(ranges, index));
        continue;
      }
      results.add(handleDifferentYear(ranges, index));
    }
    return String.join(", ", results.toArray(new String[0]));
  }

  private String handleDifferentYear(final List<TimeOffDateRange> ranges, final int index) {
    final TimeOffDateRange range = ranges.get(index);
    final LocalDate start = range.getStart();
    final LocalDate end = range.getEnd();
    String startDateString = DateUtil.formatDateTo(start, DateUtil.SIMPLE_MONTH_DAY_YEAR);
    String endDateString = DateUtil.formatDateTo(end, DateUtil.SIMPLE_MONTH_DAY_YEAR);

    if (index == 0) {
      if (isSameAsCurrentYear(start)) {
        startDateString = DateUtil.formatDateTo(start, DateUtil.SIMPLE_MONTH_DAY);
      }
      if (isSameAsCurrentYear(end) || (index + 1 < ranges.size()
          && isSameYear(end, ranges.get(index + 1).getStart()))) {
        endDateString = DateUtil.formatDateTo(end, DateUtil.SIMPLE_MONTH_DAY);
      }
      return startDateString + " - " + endDateString;
    }

    if (index == ranges.size() - 1) {
      if (isSameAsCurrentYear(end)) {
        endDateString = DateUtil.formatDateTo(end, DateUtil.SIMPLE_MONTH_DAY);
      }
      final LocalDate lastEnd = ranges.get(index - 1).getEnd();
      if (isSameMonth(lastEnd, start)) {
        startDateString = DateUtil.formatDateTo(start, DateUtil.DAY_YEAR);
        if (isSameAsCurrentYear(start)) {
          startDateString = DateUtil.formatDateTo(start, DateUtil.DAY);
        }
      }
      return startDateString + " - " + endDateString;
    }

    final LocalDate lastEnd = ranges.get(index - 1).getEnd();
    final LocalDate nextStart = ranges.get(index + 1).getStart();
    startDateString = DateUtil.formatDateTo(start, DateUtil.SIMPLE_MONTH_DAY);
    if (isSameMonth(lastEnd, start)) {
      startDateString = DateUtil.formatDateTo(start, DateUtil.DAY_YEAR);
      if (isSameAsCurrentYear(start)) {
        startDateString = DateUtil.formatDateTo(start, DateUtil.DAY);
      }
    }
    if (isSameYear(end, nextStart)) {
      endDateString = DateUtil.formatDateTo(end, DateUtil.SIMPLE_MONTH_DAY);
    }
    return startDateString + " - " + endDateString;
  }

  private String handleSameYear(final List<TimeOffDateRange> ranges, final int index) {
    final TimeOffDateRange range = ranges.get(index);
    final LocalDate start = range.getStart();
    final LocalDate end = range.getEnd();
    String startDateString = DateUtil.formatDateTo(start, DateUtil.SIMPLE_MONTH_DAY);
    String endDateString = DateUtil.formatDateTo(end, DateUtil.SIMPLE_MONTH_DAY);
    if (index == 0) {
      if (isSameAsCurrentYear(end) || (index + 1 < ranges.size()
          && isSameYear(end, ranges.get(index + 1).getStart()))) {
        return startDateString + " - " + endDateString;
      }
      if (index + 1 < ranges.size()) {
        final LocalDate nextStart = ranges.get(index + 1).getStart();
        if (!isSameYear(end, nextStart)) {
          endDateString = DateUtil.formatDateTo(end, DateUtil.SIMPLE_MONTH_DAY_YEAR);
          return startDateString + " - " + endDateString;
        }
      }
    }

    if (index == ranges.size() - 1) {
      final LocalDate lastEnd = ranges.get(index - 1).getEnd();
      if (isSameMonth(lastEnd, start)) {
        startDateString = DateUtil.formatDateTo(start, DateUtil.SIMPLE_MONTH_DAY);
      }
      if (!isSameAsCurrentYear(end)) {
        endDateString = DateUtil.formatDateTo(end, DateUtil.SIMPLE_MONTH_DAY_YEAR);
      }
      return startDateString + " - " + endDateString;
    }

    final LocalDate lastEnd = ranges.get(index - 1).getEnd();
    final LocalDate nextStart = ranges.get(index + 1).getStart();

    if (isSameMonth(lastEnd, start)) {
      startDateString = DateUtil.formatDateTo(start, DateUtil.SIMPLE_MONTH_DAY);
    }

    if (!isSameAsCurrentYear(end) && !isSameYear(end, nextStart)) {
      endDateString = DateUtil.formatDateTo(end, DateUtil.SIMPLE_MONTH_DAY_YEAR);
    }
    return startDateString + " - " + endDateString;
  }

  private String handleSameMonth(final List<TimeOffDateRange> ranges, final int index) {
    final TimeOffDateRange range = ranges.get(index);
    final LocalDate start = range.getStart();
    final LocalDate end = range.getEnd();
    if (index == 0) {
      if (isSameAsCurrentYear(start) || (index + 1 < ranges.size()
          && isSameYear(end, ranges.get(index + 1).getStart()))) {
        return DateUtil.formatDateTo(start, DateUtil.SIMPLE_MONTH_DAY) + " - "
            + DateUtil.formatDateTo(end, DateUtil.DAY);
      }
      if (index + 1 < ranges.size()) {
        final LocalDate nextStart = ranges.get(index + 1).getStart();
        if (!isSameYear(end, nextStart)) {
          return DateUtil.formatDateTo(start, DateUtil.SIMPLE_MONTH_DAY) + " - "
              + DateUtil.formatDateTo(end, DateUtil.DAY_YEAR);
        }
      }
    }

    String startDateString = DateUtil.formatDateTo(start, DateUtil.SIMPLE_MONTH_DAY);
    String endDateString = DateUtil.formatDateTo(end, DateUtil.DAY_YEAR);

    if (index == ranges.size() - 1) {
      if (isSameAsCurrentYear(end)) {
        endDateString = DateUtil.formatDateTo(end, DateUtil.DAY);
      }
      if (index - 1 >= 0) {
        final LocalDate lastEnd = ranges.get(index - 1).getEnd();
        if (isSameMonth(lastEnd, start)) {
          startDateString = DateUtil.formatDateTo(end, DateUtil.SIMPLE_MONTH_DAY);
        }
      }
      return startDateString + " - " + endDateString;
    }

    final LocalDate lastEnd = ranges.get(index - 1).getEnd();
    final LocalDate nextStart = ranges.get(index + 1).getStart();
    if (isSameAsCurrentYear(end) || isSameYear(end, nextStart)) {
      endDateString = DateUtil.formatDateTo(end, DateUtil.DAY);
    }

    if (isSameMonth(lastEnd, start)) {
      startDateString = DateUtil.formatDateTo(start, DateUtil.DAY);
    }
    return startDateString + " - " + endDateString;
  }

  private String handleSameDate(final LocalDate date, final List<TimeOffDateRange> ranges,
      final int index) {
    if (index == 0) {
      if (isSameAsCurrentYear(date)) {
        return DateUtil.formatDateTo(date, DateUtil.SIMPLE_MONTH_DAY);
      }
      if (index + 1 < ranges.size() && isSameYear(date, ranges.get(index + 1).getStart())) {
        return DateUtil.formatDateTo(date, DateUtil.SIMPLE_MONTH_DAY);
      }
      return DateUtil.formatDateTo(date, DateUtil.SIMPLE_MONTH_DAY_YEAR);
    }

    if (index == ranges.size() - 1) {
      if (isSameAsCurrentYear(date)) {
        if (index - 1 >= 0 && isSameMonth(date, ranges.get(index - 1).getEnd())) {
          return DateUtil.formatDateTo(date, DateUtil.SIMPLE_MONTH_DAY);
        }
        return DateUtil.formatDateTo(date, DateUtil.DAY);
      }
      if (index - 1 >= 0 && !isSameMonth(date, ranges.get(index - 1).getEnd())) {
        return DateUtil.formatDateTo(date, DateUtil.SIMPLE_MONTH_DAY_YEAR);
      }
      return DateUtil.formatDateTo(date, DateUtil.DAY_YEAR);
    }

    final LocalDate lastEnd = ranges.get(index - 1).getEnd();
    final LocalDate nextStart = ranges.get(index + 1).getStart();

    if (isSameAsCurrentYear(date) || isSameYear(date, nextStart)) {
      if (!isSameMonth(lastEnd, date)) {
        return DateUtil.formatDateTo(date, DateUtil.SIMPLE_MONTH_DAY);
      }
      return DateUtil.formatDateTo(date, DateUtil.DAY);
    }

    if (!isSameMonth(lastEnd, date)) {
      return DateUtil.formatDateTo(date, DateUtil.SIMPLE_MONTH_DAY_YEAR);
    }
    return DateUtil.formatDateTo(date, DateUtil.DAY_YEAR);


  }

  private List<TimeOffDateRange> splitDates(final TimeOffRequest timeOffRequest) {
    final List<TimeOffRequestDate> dates = new ArrayList<>(timeOffRequest.getTimeOffRequestDates());
    dates.sort(Comparator.comparing(TimeOffRequestDate::getDate));
    return getDateRanges(dates);
  }

  private List<TimeOffDateRange> getDateRanges(final List<TimeOffRequestDate> dates) {
    final List<TimeOffDateRange> ranges = new ArrayList<>();
    while (!dates.isEmpty()) {
      final TimeOffRequestDate start = dates.remove(0);
      final TimeOffRequestDate end = getEndDate(dates, start);
      ranges.add(new TimeOffDateRange(start, end));
    }
    return ranges;
  }


  private TimeOffRequestDate getEndDate(
      final List<TimeOffRequestDate> dates, final TimeOffRequestDate dateA) {
    if (dates.isEmpty()) {
      return dateA;
    }
    final TimeOffRequestDate dateB = dates.remove(0);
    if (isNextDate(dateA.getDate(), dateB.getDate())) {
      return getEndDate(dates, dateB);
    }
    dates.add(0, dateB);
    return dateA;
  }

  private boolean isNextDate(final Timestamp dateA, final Timestamp dateB) {
    return isSameDate(LocalDate.from(dateA.toLocalDateTime()).plusDays(1),
        LocalDate.from(dateB.toLocalDateTime()));
  }

  private boolean isSameDate(final LocalDate dateA, final LocalDate dateB) {
    return dateA.isEqual(dateB);
  }

  private boolean isSameYear(final LocalDate dateA, final LocalDate dateB) {
    return dateA.getYear() == dateB.getYear();
  }

  private boolean isSameMonth(final LocalDate dateA, final LocalDate dateB) {
    if (!isSameYear(dateA, dateB)) {
      return false;
    }
    return dateA.getMonthValue() == dateB.getMonthValue();
  }

  private boolean isSameAsCurrentYear(final LocalDate date) {
    return date.getYear() == LocalDate.now().getYear();
  }

  @Data
  private class TimeOffDateRange {

    private LocalDate start;

    private LocalDate end;

    TimeOffDateRange(final TimeOffRequestDate start, final TimeOffRequestDate end) {
      this.start = LocalDate.from(start.getDate().toLocalDateTime());
      this.end = LocalDate.from(end.getDate().toLocalDateTime());
    }
  }

}
