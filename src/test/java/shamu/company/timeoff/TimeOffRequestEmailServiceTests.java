package shamu.company.timeoff;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.thymeleaf.ITemplateEngine;
import shamu.company.common.ApplicationConfig;
import shamu.company.email.EmailService;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.service.TimeOffRequestEmailService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;

public class TimeOffRequestEmailServiceTests {

  @Mock
  private EmailService emailService;

  @Mock
  private AwsHelper awsHelper;

  @Mock
  private ITemplateEngine templateEngine;

  @Mock
  private ApplicationConfig applicationConfig;

  @Mock
  private TimeOffRequestService timeOffRequestService;

  @InjectMocks
  private TimeOffRequestEmailService timeOffRequestEmailService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testTimeOffRangeOver3Years() {
    final TimeOffRequest timeOffRequest = new TimeOffRequest();
    final Set<TimeOffRequestDate> dates = new HashSet<>();
    final int currentYear = LocalDateTime.now().getYear();
    final int lastYear = currentYear - 1;
    final int nextYear = currentYear + 1;
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(lastYear + "-10-10 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(lastYear + "-11-30 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(lastYear + "-12-01 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(lastYear + "-12-31 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-01-01 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-03-01 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-04-30 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-05-01 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-08-08 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-08-22 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-09-13 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-12-31 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-01-01 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-01-21 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-01-31 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-02-01 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-02-06 00:00:00"))));
    timeOffRequest.setTimeOffRequestDates(dates);
    final String result = timeOffRequestEmailService.getTimeOffRange(timeOffRequest);
    final String expectedTimeOffRange = "Oct 10, Nov 30 - Dec 1, 31, " + lastYear + " - Jan 1,"
        + " Mar 1, Apr 30 - May 1, Aug 8, 22, Sep 13, "
        + "Dec 31 - Jan 1, 21, Jan 31 - Feb 1, 6, " + nextYear;
    Assertions.assertEquals(expectedTimeOffRange, result);
  }

  @Test
  void testTimeOffRangeOver2Years() {
    final TimeOffRequest timeOffRequest = new TimeOffRequest();
    final Set<TimeOffRequestDate> dates = new HashSet<>();
    final int currentYear = LocalDateTime.now().getYear();
    final int nextYear = currentYear + 1;
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-11-15 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-11-22 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-07-08 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-07-10 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-08-07 00:00:00"))));
    timeOffRequest.setTimeOffRequestDates(dates);
    final String result = timeOffRequestEmailService.getTimeOffRange(timeOffRequest);
    final String expectedTimeOffRange = "Nov 15, 22, Jul 8, 10, Aug 7, " + nextYear;
    Assertions.assertEquals(expectedTimeOffRange, result);
  }

  @Test
  void testTimeOffRangeOver2YearsAndSingleDate() {
    final TimeOffRequest timeOffRequest = new TimeOffRequest();
    final Set<TimeOffRequestDate> dates = new HashSet<>();
    final int currentYear = LocalDateTime.now().getYear();
    final int nextYear = currentYear + 1;
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-12-02 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-12-03 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-12-04 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-12-20 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(currentYear + "-12-27 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-08-04 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-08-05 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-08-06 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-08-19 00:00:00"))));
    dates.add((new TimeOffRequestDate(Timestamp.valueOf(nextYear + "-08-21 00:00:00"))));
    timeOffRequest.setTimeOffRequestDates(dates);
    final String result = timeOffRequestEmailService.getTimeOffRange(timeOffRequest);
    final String expectedTimeOffRange = "Dec 2 - 4, 20, 27, Aug 4 - 6, 19, 21, " + nextYear;
    Assertions.assertEquals(expectedTimeOffRange, result);
  }

  @Nested
  class sendEmail {
    TimeOffRequest timeOffRequest;
    TimeOffRequestApprovalStatus timeOffRequestApprovalStatus;
    User user;
    User approver;
    User manger;
    TimeOffPolicy timeOffPolicy;
    Set<TimeOffRequestDate> timeOffRequestDates;
    TimeOffRequestDate timeOffRequestDate;
    UserPersonalInformation userPersonalInformation;
    UserContactInformation userContactInformation;

    @BeforeEach
    void init() {
      timeOffRequest = new TimeOffRequest();
      timeOffRequestApprovalStatus = new TimeOffRequestApprovalStatus();
      user = new User();
      approver = new User();
      timeOffPolicy = new TimeOffPolicy();
      timeOffRequestDates = new LinkedHashSet<>();
      timeOffRequestDate = new TimeOffRequestDate();
      userPersonalInformation = new UserPersonalInformation();
      userContactInformation = new UserContactInformation();
      manger = new User();

      userPersonalInformation.setFirstName("007");
      userPersonalInformation.setPreferredName("007");
      userPersonalInformation.setLastName("007");

      user.setId("1");
      approver.setId("2");
      user.setUserPersonalInformation(userPersonalInformation);
      user.setUserContactInformation(userContactInformation);
      approver.setUserPersonalInformation(userPersonalInformation);
      approver.setUserContactInformation(userContactInformation);
      manger.setId("3");
      manger.setUserContactInformation(userContactInformation);
      manger.setUserPersonalInformation(userPersonalInformation);
      user.setManagerUser(manger);

      timeOffRequestDate.setHours(10);
      timeOffRequestDate.setDate(Timestamp.valueOf(LocalDateTime.now()));

      timeOffPolicy.setIsLimited(true);

      timeOffRequestDates.add(timeOffRequestDate);

      timeOffRequest.setRequesterUser(user);
      timeOffRequest.setApproverUser(approver);
      timeOffRequest.setBalance(100);
      timeOffRequest.setTimeOffRequestDates(timeOffRequestDates);
      timeOffRequest.setTimeOffPolicy(timeOffPolicy);
    }

    @Test
    void whenStatusIsDenied_thenShouldSuccess() {
      timeOffRequestApprovalStatus.setName(TimeOffRequestApprovalStatus.TimeOffApprovalStatus.DENIED.name());
      timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

      Mockito.when(timeOffRequestService.findByRequestId(Mockito.any())).thenReturn(timeOffRequest);
      Mockito.when(templateEngine.process(Mockito.eq("time_off_request_pending.html"), Mockito.any())).thenReturn("");

      Assertions.assertDoesNotThrow(() ->
          timeOffRequestEmailService.sendEmail(timeOffRequest));
    }

    @Test
    void whenStatusIsApprove_thenShouldSuccess() {
      timeOffRequestApprovalStatus.setName(TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED.name());
      timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

      Mockito.when(timeOffRequestService.findByRequestId(Mockito.any())).thenReturn(timeOffRequest);
      Mockito.when(templateEngine.process(Mockito.eq("time_off_request_approve.html"), Mockito.any())).thenReturn("");

      Assertions.assertDoesNotThrow(() ->
          timeOffRequestEmailService.sendEmail(timeOffRequest));
    }

    @Test
    void whenStatusIsPending_thenShouldSuccess() {
      timeOffRequestApprovalStatus.setName(TimeOffRequestApprovalStatus.TimeOffApprovalStatus.AWAITING_REVIEW.name());
      timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

      Mockito.when(timeOffRequestService.findByRequestId(Mockito.any())).thenReturn(timeOffRequest);
      Mockito.when(templateEngine.process(Mockito.eq("time_off_request_pending.html"), Mockito.any())).thenReturn("");

      Assertions.assertDoesNotThrow(() ->
          timeOffRequestEmailService.sendEmail(timeOffRequest));
    }
  }

}
