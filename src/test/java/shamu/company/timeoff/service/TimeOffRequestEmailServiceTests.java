package shamu.company.timeoff.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
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
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestComment;
import shamu.company.timeoff.entity.TimeOffRequestDate;
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

  @Mock private TimeOffDetailService timeOffDetailService;

  @Mock private TimeOffPolicyUserService timeOffPolicyUserService;

  @InjectMocks private TimeOffRequestEmailService timeOffRequestEmailService;

  @Mock private Auth0Helper auth0Helper;

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
      Mockito.when(applicationConfig.getSystemEmailAddress()).thenReturn("no-reply@emailAddress");
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

      Mockito.when(auth0Helper.isIndeedEnvironment()).thenReturn(false);

      assertThatCode(() -> timeOffRequestEmailService.sendEmail(timeOffRequest))
          .doesNotThrowAnyException();
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

      Mockito.when(auth0Helper.isIndeedEnvironment()).thenReturn(false);

      assertThatCode(() -> timeOffRequestEmailService.sendEmail(timeOffRequest))
          .doesNotThrowAnyException();
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
      Mockito.when(auth0Helper.isIndeedEnvironment()).thenReturn(false);

      assertThatCode(() -> timeOffRequestEmailService.sendEmail(timeOffRequest))
          .doesNotThrowAnyException();
    }

    @Test
    void whenManagerEditRequest_thenShouldSuccess() {
      final TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
          new TimeOffRequestApprovalStatus();
      timeOffRequestApprovalStatus.setName(TimeOffApprovalStatus.APPROVED.name());
      timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

      assertThatCode(() -> timeOffRequestEmailService.sendManagerEditedRequestEmail(timeOffRequest))
          .doesNotThrowAnyException();
    }

    @AfterEach
    void testDefaultAvatarName() throws Exception {
      final Map<String, Object> variables =
          Whitebox.invokeMethod(
              timeOffRequestEmailService, "getVariablesOfTimeOffRequestEmail", timeOffRequest);
      assertThat(variables.get("avatarText")).isEqualTo("HK");
    }
  }

  @Nested
  class TestSendDeleteRequestEmail {

    private TimeOffRequest timeOffRequest;

    private TimeOffPolicy timeOffPolicy;

    private TimeOffPolicyUser timeOffPolicyUser;

    private TimeOffBreakdownDto timeOffBreakdownDto;

    private UserPersonalInformation requesterPersonalInformation;

    private UserPersonalInformation approverPersonalInformation;

    private UserContactInformation approverContactInformation;

    private User requester;

    private User approver;

    @BeforeEach
    void init() {
      timeOffPolicy = new TimeOffPolicy();
      timeOffPolicy.setId(UuidUtil.getUuidString());
      timeOffPolicy.setIsLimited(true);

      timeOffPolicyUser = new TimeOffPolicyUser();
      timeOffPolicyUser.setId(UuidUtil.getUuidString());
      timeOffPolicyUser.setTimeOffPolicy(timeOffPolicy);
      timeOffPolicyUser.setUser(requester);

      timeOffRequest = new TimeOffRequest();
      timeOffRequest.setId(UuidUtil.getUuidString());
      timeOffRequest.setTimeOffPolicy(timeOffPolicy);

      requesterPersonalInformation = new UserPersonalInformation();
      requesterPersonalInformation.setId(UuidUtil.getUuidString());
      requesterPersonalInformation.setFirstName("re");
      requesterPersonalInformation.setLastName("df");

      requester = new User(UuidUtil.getUuidString());
      requester.setUserPersonalInformation(requesterPersonalInformation);

      approverPersonalInformation = new UserPersonalInformation();
      approverPersonalInformation.setId(UuidUtil.getUuidString());
      approverPersonalInformation.setFirstName("zxc");
      approverPersonalInformation.setLastName("asd");

      approverContactInformation = new UserContactInformation();
      approverContactInformation.setId(UuidUtil.getUuidString());
      approverContactInformation.setEmailWork("qwe@asd.zxc");

      approver = new User(UuidUtil.getUuidString());
      approver.setUserPersonalInformation(approverPersonalInformation);
      approver.setUserContactInformation(approverContactInformation);

      timeOffRequest.setRequesterUser(requester);
      timeOffRequest.setApproverUser(approver);

      timeOffBreakdownDto = new TimeOffBreakdownDto();
      timeOffBreakdownDto.setBalance(1);
      Mockito.when(applicationConfig.getSystemEmailAddress()).thenReturn("no-reply@emailAddress");
    }

    @Test
    void whenStatusIsNotApproved_thenShouldDoNothing() {
      final TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
          new TimeOffRequestApprovalStatus();
      timeOffRequestApprovalStatus.setName(TimeOffApprovalStatus.AWAITING_REVIEW.name());
      timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

      assertThatCode(() -> timeOffRequestEmailService.sendDeleteRequestEmail(timeOffRequest))
          .doesNotThrowAnyException();
    }

    @Test
    void whenStatusIsApproved_thenShouldSuccess() {
      final TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
          new TimeOffRequestApprovalStatus();
      timeOffRequestApprovalStatus.setName(TimeOffApprovalStatus.APPROVED.name());
      timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

      Mockito.when(timeOffRequestService.findByRequestId(timeOffRequest.getId()))
          .thenReturn(timeOffRequest);
      Mockito.when(timeOffPolicyUserService.findByUserAndTimeOffPolicy(requester, timeOffPolicy))
          .thenReturn(timeOffPolicyUser);
      Mockito.when(timeOffDetailService.getTimeOffBreakdown(Mockito.anyString(), Mockito.anyLong()))
          .thenReturn(timeOffBreakdownDto);

      Mockito.when(
              templateEngine.process(Mockito.eq("time_off_request_delete.html"), Mockito.any()))
          .thenReturn("");

      Mockito.when(auth0Helper.isIndeedEnvironment()).thenReturn(false);

      assertThatCode(() -> timeOffRequestEmailService.sendDeleteRequestEmail(timeOffRequest))
          .doesNotThrowAnyException();
    }

    @Test
    void whenManagerDeleteRequest_thenShouldSuccess() {
      final TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
          new TimeOffRequestApprovalStatus();
      timeOffRequestApprovalStatus.setName(TimeOffApprovalStatus.APPROVED.name());
      timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

      UserContactInformation requesterConcatInfo = new UserContactInformation();
      requesterConcatInfo.setId(UuidUtil.getUuidString());
      requesterConcatInfo.setEmailWork("qwe@qq.com");
      requester.setUserContactInformation(requesterConcatInfo);

      Mockito.when(timeOffRequestService.findByRequestId(timeOffRequest.getId()))
          .thenReturn(timeOffRequest);
      Mockito.when(
          templateEngine.process(Mockito.eq("time_off_request_delete.html"), Mockito.any()))
          .thenReturn("");
      Mockito.when(timeOffPolicyUserService.findByUserAndTimeOffPolicy(requester, timeOffPolicy))
          .thenReturn(timeOffPolicyUser);
      Mockito.when(timeOffDetailService.getTimeOffBreakdown(Mockito.anyString(), Mockito.anyLong()))
          .thenReturn(timeOffBreakdownDto);
      assertThatCode(() -> timeOffRequestEmailService.sendManagerDeleteRequestEmail(timeOffRequest))
          .doesNotThrowAnyException();
    }
  }
}
