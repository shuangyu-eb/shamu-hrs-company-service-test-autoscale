package shamu.company.timeoff;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.server.dto.AuthUser;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffRequestCreateDto;
import shamu.company.timeoff.dto.TimeOffRequestDateDto;
import shamu.company.timeoff.dto.TimeOffRequestDetailDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestUpdateDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.entity.mapper.TimeOffRequestMapper;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestApprovalStatusRepository;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.timeoff.service.TimeOffDetailService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.timeoff.service.TimeOffPolicyUserService;
import shamu.company.timeoff.service.TimeOffRequestApprovalStatusService;
import shamu.company.timeoff.service.TimeOffRequestDateService;
import shamu.company.timeoff.service.TimeOffRequestEmailService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserRole;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserService;
import shamu.company.utils.UuidUtil;

public class TimeOffRequestServiceTests {

  @InjectMocks private TimeOffRequestService timeOffRequestService;

  @Mock private TimeOffRequestRepository timeOffRequestRepository;

  @Mock private TimeOffPolicyUserService timeOffPolicyUserService;

  @Mock private TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  @Mock private UserRepository userRepository;

  @Mock private TimeOffRequestEmailService timeOffRequestEmailService;

  @Mock private TimeOffRequestMapper timeOffRequestMapper;

  @Mock private TimeOffPolicyService timeOffPolicyService;

  @Mock private TimeOffDetailService timeOffDetailService;

  @Mock private Auth0Helper auth0Helper;

  @Mock private UserService userService;

  @Mock private TimeOffRequestApprovalStatusRepository requestApprovalStatusRepository;

  @Mock private TimeOffRequestDateService timeOffRequestDateService;

  @Mock private TimeOffRequestApprovalStatusService timeOffRequestApprovalStatusService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void findByRequestId() {
    final TimeOffRequest timeOffRequest = new TimeOffRequest();

    Mockito.when(timeOffRequestRepository.findByRequestId(Mockito.any()))
        .thenReturn(timeOffRequest);

    Assertions.assertDoesNotThrow(() -> timeOffRequestService.findByRequestId("1"));
  }

  @Test
  void getByApproverAndStatusFilteredByStartDay() {
    final TimeOffRequest timeOffRequest = new TimeOffRequest();

    Mockito.when(
            timeOffRequestRepository.findByApproversAndTimeOffApprovalStatus(
                Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(Page.empty());

    Assertions.assertDoesNotThrow(
        () ->
            timeOffRequestService.getByApproverAndStatus(
                Mockito.any(), Mockito.any(), Mockito.any()));
  }

  @Test
  void getPendingRequestsCount() {
    Mockito.when(
            timeOffRequestRepository.countByApproverIdAndTimeOffApprovalStatus(
                Mockito.any(), Mockito.any()))
        .thenReturn(1);

    Assertions.assertDoesNotThrow(
        () -> timeOffRequestService.getPendingRequestsCount(new User("1")));
  }

  @Test
  void whenNull_thenThrow() {
    Mockito.when(timeOffRequestRepository.findById(Mockito.any())).thenReturn(Optional.empty());

    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> timeOffRequestService.getById(Mockito.any()));
  }

  @Test
  void whenNotNull_thenShouldSuccess() {
    Mockito.when(timeOffRequestRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new TimeOffRequest()));

    Assertions.assertDoesNotThrow(() -> timeOffRequestService.getById(Mockito.any()));
  }

  @Test
  void save() {
    Mockito.when(timeOffRequestRepository.save(Mockito.any())).thenReturn(new TimeOffRequest());

    Assertions.assertDoesNotThrow(() -> timeOffRequestService.save(Mockito.any()));
  }

  @Test
  void createTimeOffRequest() {
    Mockito.when(timeOffRequestRepository.save(Mockito.any())).thenReturn(new TimeOffRequest());

    Assertions.assertDoesNotThrow(() -> timeOffRequestService.createTimeOffRequest(Mockito.any()));
  }

  @Test
  void findRecentRequestByRequesterAndStatus() {
    Mockito.when(
            timeOffRequestRepository.findRecentApprovedRequestByRequesterUserId(
                Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new TimeOffRequest());
    Mockito.when(timeOffRequestMapper.convertToTimeOffRequestDto(Mockito.any()))
        .thenReturn(new TimeOffRequestDto());

    Assertions.assertDoesNotThrow(
        () ->
            timeOffRequestService.findRecentRequestByRequesterAndStatus(
                Mockito.any(), Mockito.any(), Mockito.any()));
  }

  @Test
  void updateTimeOffRequestStatus() {
    final TimeOffRequest timeOffRequest = new TimeOffRequest();
    final TimeOffRequestUpdateDto updateDto = new TimeOffRequestUpdateDto();
    final AuthUser authUser = new AuthUser();
    final TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
        new TimeOffRequestApprovalStatus();
    final TimeOffRequestDto timeOffRequestDto = new TimeOffRequestDto();

    timeOffRequestApprovalStatus.setName(TimeOffApprovalStatus.APPROVED.name());

    timeOffRequest.setId("1");
    timeOffRequest.setApproverUser(new User("1"));
    timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

    authUser.setId("1");

    updateDto.setStatus(TimeOffApprovalStatus.DENIED);
    updateDto.setApproverComment("007");

    timeOffRequestDto.setComment(updateDto.getApproverComment());
    timeOffRequestDto.setStatus(updateDto.getStatus().name());

    Mockito.when(timeOffRequestMapper.createFromTimeOffRequestUpdateDto(Mockito.any()))
        .thenReturn(timeOffRequest);
    Mockito.when(timeOffRequestRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(timeOffRequest));
    Mockito.when(requestApprovalStatusRepository.findByName(Mockito.any()))
        .thenReturn(timeOffRequestApprovalStatus);
    Mockito.when(timeOffRequestApprovalStatusService.findByName(Mockito.any()))
        .thenReturn(timeOffRequestApprovalStatus);
    Mockito.when(timeOffRequestRepository.save(Mockito.any())).thenReturn(timeOffRequest);
    Mockito.when(timeOffRequestMapper.convertToTimeOffRequestDto(timeOffRequest))
        .thenReturn(timeOffRequestDto);

    Assertions.assertDoesNotThrow(
        () -> timeOffRequestService.updateTimeOffRequestStatus("1", updateDto, authUser));
    Assertions.assertEquals(
        timeOffRequestService.updateTimeOffRequestStatus("1", updateDto, authUser).getComment(),
        updateDto.getApproverComment());
    Assertions.assertEquals(
        timeOffRequestService.updateTimeOffRequestStatus("1", updateDto, authUser).getStatus(),
        updateDto.getStatus().name());
  }

  @Test
  void deleteUnimplementedRequest() {
    Assertions.assertDoesNotThrow(() -> timeOffRequestService.deleteUnimplementedRequest("1"));
  }

  @Test
  void setTimeOffDetailDtoIsCurrentUserPrivileged() {
    final AuthUser authUser = new AuthUser();
    final User user = new User();
    user.setManagerUser(new User("1"));

    authUser.setRole(Role.EMPLOYEE);
    authUser.setId("1");

    Assertions.assertDoesNotThrow(
        () ->
            Whitebox.invokeMethod(
                timeOffRequestService,
                "setTimeOffDetailDtoIsCurrentUserPrivileged",
                user,
                authUser,
                new TimeOffRequestDetailDto()));
  }

  @Test
  void saveTimeOffRequest() {
    final TimeOffRequest timeOffRequest = new TimeOffRequest();
    final TimeOffRequestCreateDto requestCreateDto = new TimeOffRequestCreateDto();
    final User approver = new User();
    final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
    final TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
        new TimeOffRequestApprovalStatus();
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    final List<TimeOffRequestDate> timeOffRequestDates = new ArrayList<>();
    final TimeOffRequestDate timeOffRequestDate = new TimeOffRequestDate();
    final List<TimeOffRequestDateDto> timeOffRequestDateDtos = new ArrayList<>();
    final TimeOffRequestDateDto timeOffRequestDateDto = new TimeOffRequestDateDto();

    timeOffRequestDateDtos.add(timeOffRequestDateDto);

    timeOffRequestDates.add(timeOffRequestDate);

    timeOffBreakdownDto.setBalance(7);

    timeOffRequestApprovalStatus.setName(TimeOffApprovalStatus.AWAITING_REVIEW.name());

    requestCreateDto.setPolicyId("1");
    requestCreateDto.setDates(timeOffRequestDateDtos);

    timeOffPolicyUser.setId("1");
    timeOffPolicyUser.setUser(new User("1"));
    timeOffPolicyUser.setTimeOffPolicy(timeOffPolicy);

    Mockito.when(timeOffPolicyService.getTimeOffPolicyById(Mockito.any()))
        .thenReturn(timeOffPolicy);
    Mockito.when(timeOffRequestApprovalStatusService.findByName(Mockito.any()))
        .thenReturn(timeOffRequestApprovalStatus);
    Mockito.when(timeOffPolicyUserService.findByUserAndTimeOffPolicy(Mockito.any(), Mockito.any()))
        .thenReturn(timeOffPolicyUser);
    Mockito.when(timeOffDetailService.getTimeOffBreakdown(Mockito.any(), Mockito.any()))
        .thenReturn(timeOffBreakdownDto);
    Mockito.when(timeOffRequestRepository.save(Mockito.any())).thenReturn(timeOffRequest);
    Mockito.when(timeOffRequestDateService.saveAllTimeOffRequestDates(Mockito.any()))
        .thenReturn(timeOffRequestDates);

    Assertions.assertDoesNotThrow(
        () ->
            timeOffRequestService.saveTimeOffRequest(
                timeOffRequest, requestCreateDto, TimeOffApprovalStatus.AWAITING_REVIEW, approver));
  }

  @Test
  void findTimeOffRequests() {
    final User user = new User();
    final TimeOffApprovalStatus[] statuses =
        new TimeOffApprovalStatus[] {TimeOffApprovalStatus.AWAITING_REVIEW};

    Mockito.when(userService.findById(Mockito.any())).thenReturn(user);

    Assertions.assertDoesNotThrow(() -> timeOffRequestService.findTimeOffRequests("1", statuses));
  }

  @Nested
  class getRequestsByUserAndStatus {

    private User user;

    private User manager;

    private TimeOffApprovalStatus[] statuses;

    private UserRole userRole;

    @BeforeEach
    void setUp() {
      user = new User();
      user.setId("1");
      manager = new User();
      manager.setId("2");
      userRole = new UserRole();
      statuses = new TimeOffApprovalStatus[1];
      statuses[0] = TimeOffApprovalStatus.APPROVED;
    }

    @Test
    void whenUserHasNoManager_thenFindAdminTeamRequests() {
      user.setManagerUser(null);
      timeOffRequestService.getRequestsByUserAndStatus(user, statuses);
      Mockito.verify(timeOffRequestRepository, Mockito.times(1))
          .findAdminTeamRequestsByRoles(Mockito.any(), Mockito.any(), Mockito.anyList());
    }

    @Test
    void whenUserRoleIsManagerOrAdmin_thenFindManagerTeamRequests() {
      user.setManagerUser(manager);
      userRole.setName(String.valueOf(Role.MANAGER));
      userRole.setId("1");
      user.setUserRole(userRole);
      Mockito.when(auth0Helper.getUserRole(user)).thenReturn(Role.MANAGER);
      timeOffRequestService.getRequestsByUserAndStatus(user, statuses);
      Mockito.verify(timeOffRequestRepository, Mockito.times(1))
          .findManagerTeamRequestsByRoles(
              Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyList());
    }

    @Test
    void whenUserRoleNotManagerAndNotAdmin_thenFindEmployeeTeamRequests() {
      user.setManagerUser(manager);
      userRole.setName(String.valueOf(Role.EMPLOYEE));
      userRole.setId("1");
      user.setUserRole(userRole);
      Mockito.when(auth0Helper.getUserRole(user)).thenReturn(Role.EMPLOYEE);
      timeOffRequestService.getRequestsByUserAndStatus(user, statuses);
      Mockito.verify(timeOffRequestRepository, Mockito.times(1))
          .findEmployeeSelfPendingRequests(Mockito.any(), Mockito.any());
    }
  }

  @Nested
  class initMyTimeOff {

    private Boolean filteredByEndDay;

    private Page<TimeOffRequest> pageRequest;

    private String[] statuses;

    @BeforeEach
    void setUp() {
      filteredByEndDay = false;
      final TimeOffRequest timeOffRequest = new TimeOffRequest();
      final List<TimeOffRequest> timeOffRequests = new LinkedList<>();
      timeOffRequests.add(timeOffRequest);
      timeOffRequest.setId("1");
      pageRequest = new PageImpl(timeOffRequests);
      statuses = new String[1];
      Mockito.when(timeOffPolicyUserService.existsByUserId(Mockito.anyString())).thenReturn(true);
      Mockito.when(
              timeOffRequestRepository.findByRequesterUserIdFilteredByStartDay(
                  Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
          .thenReturn(pageRequest);
      Mockito.when(
              timeOffRequestRepository.findByRequesterUserIdFilteredByStartAndEndDay(
                  Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
          .thenReturn(pageRequest);
    }

    @Test
    void whenFilteredByEndDayIsFalse_thenFindByRequesterUserIdFilteredByStartAndEndDay()
        throws Exception {
      final PageRequest pageRequest = PageRequest.of(0, 1);
      Whitebox.invokeMethod(
          timeOffRequestService,
          "getTimeOffDtos",
          "1",
          null,
          null,
          filteredByEndDay,
          statuses,
          pageRequest);
      Mockito.verify(timeOffRequestRepository, Mockito.times(1))
          .findByRequesterUserIdFilteredByStartDay(
              Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void whenFilteredByEndDayIsTrue_thenFindByRequesterUserIdFilteredByStartAndEndDay()
        throws Exception {
      filteredByEndDay = true;
      final PageRequest pageRequest = PageRequest.of(0, 1);
      Whitebox.invokeMethod(
          timeOffRequestService,
          "getTimeOffDtos",
          "1",
          null,
          null,
          filteredByEndDay,
          statuses,
          pageRequest);
      Mockito.verify(timeOffRequestRepository, Mockito.times(1))
          .findByRequesterUserIdFilteredByStartAndEndDay(
              Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }
  }

  @Nested
  class findReviewedRequests {
    Page<TimeOffRequest> timeOffRequests;
    List<TimeOffRequest> timeOffRequestList;
    TimeOffRequest timeOffRequest;
    PageRequest request;

    @BeforeEach
    void init() {
      timeOffRequestList = new ArrayList<>();
      timeOffRequest = new TimeOffRequest();
      timeOffRequestList.add(timeOffRequest);
      request =
          PageRequest.of(
              1,
              10,
              Sort.by(TimeOffRequestService.SortFields.APPROVED_DATE.getValue()).descending());
      timeOffRequests = new PageImpl(timeOffRequestList, request, 10);
    }

    @Test
    void whenStartDayIsNullAndEndDayIsNull_thenShouldSuccess() {

      Mockito.when(
              timeOffRequestRepository.findByRequesterUserIdFilteredByStartDay(
                  Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
          .thenReturn(timeOffRequests);
      Mockito.when(timeOffRequestMapper.convertToTimeOffRequestDto(Mockito.any()))
          .thenReturn(new TimeOffRequestDto());

      Assertions.assertDoesNotThrow(
          () -> timeOffRequestService.findReviewedRequests("1", null, null, 1, 10));
    }

    @Test
    void whenStartDayIsNotNullAndEndDayIsNotNull_thenShouldSuccess() {

      Mockito.when(
              timeOffRequestRepository.findByRequesterUserIdFilteredByStartAndEndDay(
                  Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
          .thenReturn(timeOffRequests);
      Mockito.when(timeOffRequestMapper.convertToTimeOffRequestDto(Mockito.any()))
          .thenReturn(new TimeOffRequestDto());

      Assertions.assertDoesNotThrow(
          () -> timeOffRequestService.findReviewedRequests("1", 10L, 10L, 1, 10));
    }
  }

  @Nested
  class getOtherRequestsBy {
    TimeOffRequest timeOffRequest;
    User requestUser;
    User approveUser;
    Set<TimeOffRequestDate> timeOffRequestDates;
    TimeOffRequestDate timeOffRequestDate;
    TimeOffRequestApprovalStatus timeOffRequestApprovalStatus;
    List<TimeOffRequest> timeOffRequests;
    List<User> requesters;

    @BeforeEach
    void init() {
      timeOffRequest = new TimeOffRequest();
      requestUser = new User(UuidUtil.getUuidString());
      approveUser = new User(UuidUtil.getUuidString());
      timeOffRequestDates = new HashSet<>();
      timeOffRequestDate = new TimeOffRequestDate();
      timeOffRequestApprovalStatus = new TimeOffRequestApprovalStatus();
      timeOffRequests = new ArrayList<>();
      requesters = new ArrayList<>();

      timeOffRequestApprovalStatus.setName(TimeOffApprovalStatus.APPROVED.name());

      timeOffRequest.setId("1");
      timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

      requesters.add(requestUser);
    }

    @Test
    void whenNotFirstDayAndManagerIsNull_thenShouldSuccess() {
      timeOffRequestDate.setDate(Timestamp.valueOf(LocalDateTime.now()));
      timeOffRequestDates.add(timeOffRequestDate);
      timeOffRequest.setTimeOffRequestDates(timeOffRequestDates);
      timeOffRequest.setRequesterUser(requestUser);
      timeOffRequests.add(timeOffRequest);

      Mockito.when(
              timeOffRequestRepository.findByRequesterUserInAndTimeOffApprovalStatus(
                  Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
          .thenReturn(timeOffRequests);

      Assertions.assertDoesNotThrow(() -> timeOffRequestService.getOtherRequestsBy(timeOffRequest));
    }

    @Test
    void whenFirstDayAndManagerIsNotNull_thenShouldSuccess() {
      final Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date());
      calendar.set(Calendar.DAY_OF_MONTH, 1);
      calendar.add(Calendar.MONTH, 0);

      timeOffRequestDate.setDate(new Timestamp(calendar.getTime().getTime()));
      timeOffRequestDates.add(timeOffRequestDate);
      timeOffRequest.setTimeOffRequestDates(timeOffRequestDates);
      requestUser.setManagerUser(new User(UuidUtil.getUuidString()));
      timeOffRequest.setRequesterUser(requestUser);
      timeOffRequests.add(timeOffRequest);

      Mockito.when(
              timeOffRequestRepository.findByRequesterUserInAndTimeOffApprovalStatus(
                  Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
          .thenReturn(timeOffRequests);
      Mockito.when(userService.findDirectReportsByManagerId(Mockito.any())).thenReturn(requesters);

      Assertions.assertDoesNotThrow(() -> timeOffRequestService.getOtherRequestsBy(timeOffRequest));
    }
  }

  @Nested
  class findTimeOffRequestDetail {
    TimeOffRequest timeOffRequest;
    TimeOffRequestDetailDto timeOffRequestDetailDto;
    AuthUser authUser;
    Set<TimeOffRequestDate> timeOffRequestDates;
    TimeOffRequestDate timeOffRequestDate;
    TimeOffRequestApprovalStatus timeOffRequestApprovalStatus;
    TimeOffPolicy timeOffPolicy;
    TimeOffPolicyUser timeOffPolicyUser;
    TimeOffBreakdownDto timeOffBreakdownDto;

    @BeforeEach
    void init() {
      timeOffRequest = new TimeOffRequest();
      timeOffRequestDetailDto = new TimeOffRequestDetailDto();
      authUser = new AuthUser();
      timeOffRequestDates = new HashSet<>();
      timeOffRequestDate = new TimeOffRequestDate();
      timeOffRequestApprovalStatus = new TimeOffRequestApprovalStatus();
      timeOffPolicy = new TimeOffPolicy();
      timeOffPolicyUser = new TimeOffPolicyUser();
      timeOffBreakdownDto = new TimeOffBreakdownDto();
      timeOffRequestApprovalStatus.setName(TimeOffApprovalStatus.APPROVED.name());

      timeOffRequestDate.setDate(Timestamp.valueOf(LocalDateTime.now()));
      timeOffRequestDate.setHours(8);
      timeOffRequestDates.add(timeOffRequestDate);
      timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

      timeOffRequest.setRequesterUser(new User(UuidUtil.getUuidString()));
      timeOffRequest.setTimeOffRequestDates(timeOffRequestDates);
      timeOffBreakdownDto.setBalance(100);
      timeOffRequest.setTimeOffPolicy(timeOffPolicy);
      timeOffPolicy.setId(UuidUtil.getUuidString());
      timeOffPolicyUser.setId(UuidUtil.getUuidString());
      timeOffPolicyUser.setTimeOffPolicy(timeOffPolicy);
    }

    @Test
    void whenPolicyIsLimited_thenShouldHaveBalance() {
      timeOffPolicy.setIsLimited(true);
      Mockito.when(timeOffRequestRepository.findById(Mockito.any()))
          .thenReturn(Optional.of(timeOffRequest));
      Mockito.when(timeOffRequestMapper.convertToTimeOffRequestDetailDto(Mockito.any()))
          .thenReturn(timeOffRequestDetailDto);
      Mockito.when(
              timeOffPolicyUserService.findByUserAndTimeOffPolicy(Mockito.any(), Mockito.any()))
          .thenReturn(timeOffPolicyUser);
      Mockito.when(timeOffDetailService.getTimeOffBreakdown(Mockito.anyString(), Mockito.anyLong()))
          .thenReturn(timeOffBreakdownDto);

      timeOffRequestService.findTimeOffRequestDetail("1", authUser);
      Mockito.verify(timeOffPolicyService, Mockito.times(1))
          .getTimeOffRequestHoursFromStatus(
              Mockito.anyString(),
              Mockito.anyString(),
              Mockito.any(),
              Mockito.any(),
              Mockito.any());
    }

    @Test
    void whenPolicyIsUnlimited_thenShouldNotCalculateBalance() {
      timeOffPolicy.setIsLimited(false);
      Mockito.when(timeOffRequestRepository.findById(Mockito.any()))
          .thenReturn(Optional.of(timeOffRequest));
      Mockito.when(timeOffRequestMapper.convertToTimeOffRequestDetailDto(Mockito.any()))
          .thenReturn(timeOffRequestDetailDto);
      Mockito.when(
              timeOffPolicyUserService.findByUserAndTimeOffPolicy(Mockito.any(), Mockito.any()))
          .thenReturn(timeOffPolicyUser);
      Mockito.when(timeOffDetailService.getTimeOffBreakdown(Mockito.anyString(), Mockito.anyLong()))
          .thenReturn(timeOffBreakdownDto);

      timeOffRequestService.findTimeOffRequestDetail("1", authUser);
      Mockito.verify(timeOffPolicyService, Mockito.times(0))
          .getTimeOffRequestHoursFromStatus(
              Mockito.anyString(),
              Mockito.anyString(),
              Mockito.any(),
              Mockito.any(),
              Mockito.any());
    }
  }
}
