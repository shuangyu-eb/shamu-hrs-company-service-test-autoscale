package shamu.company.timeoff;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.ap.internal.util.Collections;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.thymeleaf.ITemplateEngine;
import shamu.company.common.ApplicationConfig;
import shamu.company.email.service.EmailService;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestComment;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.service.TimeOffDetailService;
import shamu.company.timeoff.service.TimeOffRequestEmailService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.utils.UuidUtil;

public class TimeOffRequestEmailServiceTests {

  @Mock private EmailService emailService;

  @Mock private AwsHelper awsHelper;

  @Mock private ITemplateEngine templateEngine;

  @Mock private ApplicationConfig applicationConfig;

  @Mock private TimeOffRequestService timeOffRequestService;

  @InjectMocks private TimeOffRequestEmailService timeOffRequestEmailService;

  @Mock private TimeOffDetailService timeOffDetailService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
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

      userPersonalInformation.setFirstName("huang");
      userPersonalInformation.setPreferredName("007");
      userPersonalInformation.setLastName("kang");

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

      timeOffRequest.setId(UuidUtil.getUuidString());
      timeOffRequest.setRequesterUser(user);
      timeOffRequest.setApproverUser(approver);
      timeOffRequest.setBalance(100);
      timeOffRequest.setTimeOffRequestDates(timeOffRequestDates);
      timeOffRequest.setTimeOffPolicy(timeOffPolicy);

      final TimeOffRequestComment requestComment = new TimeOffRequestComment();
      requestComment.setComment("Request comment");
      requestComment.setUser(user);

      final TimeOffRequestComment approveComment = new TimeOffRequestComment();
      approveComment.setComment("Approve comment!");
      approveComment.setUser(new User());
      approveComment.getUser().setId(UuidUtil.getUuidString());
      final UserPersonalInformation approvePersonalInformation = new UserPersonalInformation();
      approvePersonalInformation.setFirstName("Manager");
      approvePersonalInformation.setLastName("Example");
      approveComment.getUser().setUserPersonalInformation(approvePersonalInformation);
      timeOffRequest.setComments(Collections.asSet(requestComment, approveComment));
    }

    @Test
    void whenStatusIsDenied_thenShouldSuccess() {
      timeOffRequestApprovalStatus.setName(
          TimeOffRequestApprovalStatus.TimeOffApprovalStatus.DENIED.name());
      timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

      Mockito.when(timeOffRequestService.findByRequestId(Mockito.any())).thenReturn(timeOffRequest);
      Mockito.when(
              templateEngine.process(Mockito.eq("time_off_request_pending.html"), Mockito.any()))
          .thenReturn("");
      Mockito.when(timeOffDetailService.getTimeOffRequestDatesPreview(Mockito.any()))
          .thenReturn("123");

      Assertions.assertDoesNotThrow(() -> timeOffRequestEmailService.sendEmail(timeOffRequest));
    }

    @Test
    void whenStatusIsApprove_thenShouldSuccess() {
      timeOffRequestApprovalStatus.setName(
          TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED.name());
      timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

      Mockito.when(timeOffRequestService.findByRequestId(Mockito.any())).thenReturn(timeOffRequest);
      Mockito.when(
              templateEngine.process(Mockito.eq("time_off_request_approve.html"), Mockito.any()))
          .thenReturn("");
      Mockito.when(timeOffDetailService.getTimeOffRequestDatesPreview(Mockito.any()))
          .thenReturn("123");

      Assertions.assertDoesNotThrow(() -> timeOffRequestEmailService.sendEmail(timeOffRequest));
    }

    @Test
    void whenStatusIsPending_thenShouldSuccess() {
      timeOffRequestApprovalStatus.setName(
          TimeOffRequestApprovalStatus.TimeOffApprovalStatus.AWAITING_REVIEW.name());
      timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

      Mockito.when(timeOffRequestService.findByRequestId(Mockito.any())).thenReturn(timeOffRequest);
      Mockito.when(
              templateEngine.process(Mockito.eq("time_off_request_pending.html"), Mockito.any()))
          .thenReturn("");
      Mockito.when(timeOffDetailService.getTimeOffRequestDatesPreview(Mockito.any()))
          .thenReturn("123");

      Assertions.assertDoesNotThrow(() -> timeOffRequestEmailService.sendEmail(timeOffRequest));
    }

    @AfterEach
    void testDefaultAvatarName() throws Exception {
      final Map<String, Object> variables =
          Whitebox.invokeMethod(
              timeOffRequestEmailService, "getVariablesOfTimeOffRequestEmail", timeOffRequest);
      Assertions.assertEquals("HK", variables.get("avatarText"));
    }
  }
}
