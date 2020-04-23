package shamu.company.timeoff;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffAdjustmentCheckDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.dto.TimeOffPolicyFrontendDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserFrontendDto;
import shamu.company.timeoff.dto.TimeOffPolicyWrapperDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffAdjustment;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.mapper.AccrualScheduleMilestoneMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyAccrualScheduleMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyUserMapper;
import shamu.company.timeoff.pojo.TimeOffPolicyListPojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.timeoff.repository.TimeOffAdjustmentRepository;
import shamu.company.timeoff.repository.TimeOffPolicyAccrualScheduleRepository;
import shamu.company.timeoff.repository.TimeOffPolicyRepository;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.timeoff.service.TimeOffDetailService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserService;

public class TimeOffPolicyServiceTests {

  private static TimeOffPolicyService timeOffPolicyService;

  @Mock private TimeOffPolicyRepository timeOffPolicyRepository;

  @Mock private TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  @Mock private AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  @Mock private TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;

  @Mock private JobUserRepository jobUserRepository;

  @Mock private UserRepository userRepository;

  @Mock private TimeOffRequestRepository timeOffRequestRepository;

  @Mock private TimeOffAdjustmentRepository timeOffAdjustmentRepository;

  @Mock private TimeOffDetailService timeOffDetailService;

  @Mock private CompanyService companyService;

  @Mock private AccrualScheduleMilestoneMapper accrualScheduleMilestoneMapper;

  @Mock private TimeOffPolicyAccrualScheduleMapper timeOffPolicyAccrualScheduleMapper;

  @Mock private TimeOffPolicyUserMapper timeOffPolicyUserMapper;

  @Mock private TimeOffPolicyMapper timeOffPolicyMapper;

  @Mock private JobUserMapper jobUserMapper;

  @Mock private UserService userService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    timeOffPolicyService =
        new TimeOffPolicyService(
            timeOffPolicyRepository,
            timeOffPolicyUserRepository,
            accrualScheduleMilestoneRepository,
            timeOffPolicyAccrualScheduleRepository,
            jobUserRepository,
            timeOffDetailService,
            userRepository,
            timeOffRequestRepository,
            timeOffAdjustmentRepository,
            accrualScheduleMilestoneMapper,
            timeOffPolicyAccrualScheduleMapper,
            timeOffPolicyUserMapper,
            timeOffPolicyMapper,
            jobUserMapper,
            companyService,
            userService);
  }

  @Test
  void testGetTimeOffBalances() {
    final User user = new User();
    user.setId("1");
    final List<TimeOffPolicyUser> policyUsers = new ArrayList<>();
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();

    timeOffPolicyUser.setId("1");
    timeOffPolicyUser.setTimeOffPolicy(new TimeOffPolicy("1"));
    policyUsers.add(timeOffPolicyUser);
    timeOffBreakdownDto.setBalance(100);

    Mockito.when(userService.findById(Mockito.any())).thenReturn(user);
    Mockito.when(timeOffDetailService.getTimeOffBreakdown(Mockito.any(), Mockito.any()))
        .thenReturn(timeOffBreakdownDto);
    Mockito.when(timeOffPolicyUserRepository.findTimeOffPolicyUsersByUser(Mockito.any()))
        .thenReturn(policyUsers);

    Assertions.assertDoesNotThrow(() -> timeOffPolicyService.getTimeOffBalances(user.getId()));
  }

  @Test
  void testGetTimeOffRequestHoursFromStatus() {
    final List<TimeOffRequest> timeOffRequestList = new ArrayList<>();
    Mockito.when(
            timeOffRequestRepository.findByRequesterUserInAndTimeOffApprovalStatus(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(timeOffRequestList);
    Assertions.assertDoesNotThrow(
        () ->
            timeOffPolicyService.getTimeOffRequestHoursFromStatus(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()));
  }

  @Test
  void testGetTimeOffPolicyUser() {
    final User user = new User();
    user.setId("1");
    final List<TimeOffPolicyUser> policyUsers = new ArrayList<>();
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    timeOffPolicyUser.setId("1");

    timeOffPolicyUser.setId("1");
    timeOffPolicyUser.setTimeOffPolicy(new TimeOffPolicy("1"));
    policyUsers.add(timeOffPolicyUser);

    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setBalance(100);

    Mockito.when(userService.findById(Mockito.any())).thenReturn(user);
    Mockito.when(timeOffPolicyUserRepository.findTimeOffPolicyUsersByUser(Mockito.any()))
        .thenReturn(policyUsers);
    Mockito.when(timeOffDetailService.getTimeOffBreakdown(Mockito.any(), Mockito.any()))
        .thenReturn(timeOffBreakdownDto);
    Mockito.when(timeOffPolicyUserMapper.convertToTimeOffPolicyUserDto(Mockito.any()))
        .thenReturn(new TimeOffPolicyUserDto());

    Assertions.assertDoesNotThrow(() -> timeOffPolicyService.getTimeOffPolicyUser("1", 1L));
  }

  @Test
  void testCreateTimeOffPolicyUsers() {
    final List<TimeOffPolicyUser> timeOffPolicyUsers = new ArrayList<>();
    Mockito.when(timeOffPolicyUserRepository.saveAll(Mockito.any())).thenReturn(timeOffPolicyUsers);
    Assertions.assertDoesNotThrow(
        () -> timeOffPolicyService.createTimeOffPolicyUsers(Mockito.any()));
  }

  @Test
  void testGetTimeOffBalanceByUserAndPolicy() {
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    timeOffPolicyUser.setInitialBalance(100);
    Mockito.when(
            timeOffPolicyUserRepository.findTimeOffPolicyUserByUserAndTimeOffPolicy(
                Mockito.any(), Mockito.any()))
        .thenReturn(timeOffPolicyUser);
    Assertions.assertDoesNotThrow(
        () ->
            timeOffPolicyService.getTimeOffBalanceByUserAndPolicy(
                new User("1"), new TimeOffPolicy("1")));
    Assertions.assertEquals(
        String.valueOf(timeOffPolicyUser.getInitialBalance()), String.valueOf(100));
  }

  @Test
  void testGetAllEmployeesByTimeOffPolicyId() {
    final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
    timeOffPolicy.setId("1");
    timeOffPolicy.setIsLimited(false);
    final List<User> selectableTimeOffPolicyUsers = new ArrayList<>();
    final User user = new User("007");
    selectableTimeOffPolicyUsers.add(user);

    final List<TimeOffPolicyUser> timeOffPolicyUsers = new ArrayList<>();
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    timeOffPolicyUser.setId("1");
    timeOffPolicyUser.setUser(new User("1"));
    timeOffPolicyUsers.add(timeOffPolicyUser);

    final JobUser jobUser = new JobUser();

    Mockito.when(timeOffPolicyRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(timeOffPolicy));
    Mockito.when(timeOffPolicyUserRepository.findAllByTimeOffPolicyId(Mockito.any()))
        .thenReturn(timeOffPolicyUsers);
    Mockito.when(userRepository.findAllByCompanyId(Mockito.any()))
        .thenReturn(selectableTimeOffPolicyUsers);
    Mockito.when(jobUserRepository.findJobUserByUser(Mockito.any())).thenReturn(jobUser);

    Assertions.assertDoesNotThrow(
        () ->
            timeOffPolicyService.getAllEmployeesByTimeOffPolicyId(
                timeOffPolicy.getId(), new Company("1").getId()));
  }

  @Test
  void testGetAllPolicies() {
    final List<TimeOffPolicyListPojo> timeOffPolicies = new ArrayList<>();
    final TimeOffPolicyListPojo timeOffPolicyListPojo =
        new TimeOffPolicyListPojo() {
          @Override
          public String getId() {
            return "1";
          }

          @Override
          public String getName() {
            return "007";
          }

          @Override
          public Integer getEmployee() {
            return 10;
          }

          @Override
          public Boolean getIsLimited() {
            return true;
          }
        };
    timeOffPolicies.add(timeOffPolicyListPojo);

    Mockito.when(timeOffPolicyRepository.getAllPolicies(Mockito.any())).thenReturn(timeOffPolicies);
    Assertions.assertDoesNotThrow(
        () -> timeOffPolicyService.getAllPolicies(new Company("1").getId()));
  }

  @Test
  void testUpdateTimeOffPolicyMilestones() {
    final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
    final List<AccrualScheduleMilestoneDto> milestones = new ArrayList<>();
    final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
        new TimeOffPolicyAccrualSchedule();
    final AccrualScheduleMilestoneDto accrualScheduleMilestoneDto =
        new AccrualScheduleMilestoneDto();
    final AccrualScheduleMilestone accrualScheduleMilestone = new AccrualScheduleMilestone();

    accrualScheduleMilestoneDto.setName("007");
    accrualScheduleMilestoneDto.setMaxBalance(10);

    milestones.add(accrualScheduleMilestoneDto);

    timeOffPolicyAccrualSchedule.setId("1");
    timeOffPolicyAccrualSchedule.setMaxBalance(10);

    List<AccrualScheduleMilestone> accrualScheduleMilestones = new ArrayList<>();

    accrualScheduleMilestone.setId("1");
    accrualScheduleMilestone.setMaxBalance(10);
    accrualScheduleMilestone.setAccrualHours(100);
    accrualScheduleMilestone.setCarryoverLimit(150);
    accrualScheduleMilestones.add(accrualScheduleMilestone);

    Mockito.when(timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(Mockito.any()))
        .thenReturn(timeOffPolicyAccrualSchedule);
    Mockito.when(
            accrualScheduleMilestoneMapper
                .createFromAccrualScheduleMilestoneDtoAndTimeOffPolicyAccrualScheduleId(
                    Mockito.any(), Mockito.any()))
        .thenReturn(accrualScheduleMilestone);
    Mockito.when(
            accrualScheduleMilestoneRepository.findByTimeOffPolicyAccrualScheduleId(Mockito.any()))
        .thenReturn(accrualScheduleMilestones);

    Assertions.assertDoesNotThrow(
        () -> timeOffPolicyService.updateTimeOffPolicyMilestones(timeOffPolicy, milestones));

    accrualScheduleMilestones =
        timeOffPolicyService.updateTimeOffPolicyMilestones(timeOffPolicy, milestones);

    Assertions.assertEquals(
        accrualScheduleMilestones.get(0).getId(), accrualScheduleMilestone.getId());
    Assertions.assertEquals(
        accrualScheduleMilestones.get(0).getMaxBalance(), accrualScheduleMilestone.getMaxBalance());
  }

  @Test
  void testAddTimeOffAdjustments_whenExceedIsTrue_thenShouldThrow() {
    final TimeOffAdjustmentCheckDto checkResult = new TimeOffAdjustmentCheckDto();
    checkResult.setExceed(true);
    checkResult.setMaxBalance(100);
    Mockito.when(timeOffDetailService.checkTimeOffAdjustments(Mockito.any(), Mockito.any()))
        .thenReturn(checkResult);
    Assertions.assertThrows(
        ForbiddenException.class,
        () -> timeOffPolicyService.addTimeOffAdjustments(new User("1"), "1", 100));
  }

  @Test
  void testAddTimeOffAdjustments_whenExceedIsFalse_thenShouldSuccess() {
    final UserPersonalInformation personalInformation = new UserPersonalInformation();
    personalInformation.setFirstName("007");
    personalInformation.setPreferredName("007");
    personalInformation.setLastName("007");
    final User user = new User();
    user.setId("1");
    user.setCompany(new Company("1"));
    user.setUserPersonalInformation(personalInformation);
    final TimeOffAdjustmentCheckDto checkResult = new TimeOffAdjustmentCheckDto();
    checkResult.setExceed(false);
    checkResult.setMaxBalance(100);
    Mockito.when(timeOffDetailService.checkTimeOffAdjustments(Mockito.any(), Mockito.any()))
        .thenReturn(checkResult);
    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setBalance(100);
    Mockito.when(timeOffDetailService.getTimeOffBreakdown(Mockito.any(), Mockito.any()))
        .thenReturn(timeOffBreakdownDto);
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    timeOffPolicyUser.setUser(user);
    final TimeOffPolicy timeOffPolicy = new TimeOffPolicy("1");
    timeOffPolicyUser.setTimeOffPolicy(timeOffPolicy);
    Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(timeOffPolicyUser));
    final TimeOffAdjustment timeOffAdjustment = new TimeOffAdjustment();
    timeOffAdjustment.setUser(user);
    Mockito.when(timeOffAdjustmentRepository.save(Mockito.any())).thenReturn(timeOffAdjustment);
    Assertions.assertDoesNotThrow(() -> timeOffPolicyService.addTimeOffAdjustments(user, "1", 100));
  }

  @Test
  void testUpdateTimeOffPolicyUserInfo() {
    final List<TimeOffPolicyUser> oldUsersStartBalanceList = new ArrayList<>();
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    timeOffPolicyUser.setUser(new User("1"));
    oldUsersStartBalanceList.add(timeOffPolicyUser);
    final List<TimeOffPolicyUserFrontendDto> userStatBalances = new ArrayList<>();
    final TimeOffPolicyUserFrontendDto timeOffPolicyUserFrontendDto =
        new TimeOffPolicyUserFrontendDto();
    timeOffPolicyUserFrontendDto.setUserId("1");
    userStatBalances.add(timeOffPolicyUserFrontendDto);

    Mockito.when(timeOffPolicyUserRepository.findAllByTimeOffPolicyId(Mockito.any()))
        .thenReturn(oldUsersStartBalanceList);
    Mockito.when(timeOffPolicyUserRepository.saveAll(Mockito.any()))
        .thenReturn(oldUsersStartBalanceList);
    Assertions.assertDoesNotThrow(
        () -> timeOffPolicyService.updateTimeOffPolicyUserInfo(userStatBalances, "1"));
  }

  @Test
  void testGetAllPolicyUsersByPolicyId() {
    final List<TimeOffPolicyUser> timeOffPolicyUsers = new ArrayList<>();

    Mockito.when(timeOffPolicyUserRepository.findAllByTimeOffPolicyId(Mockito.any()))
        .thenReturn(timeOffPolicyUsers);
    Assertions.assertDoesNotThrow(() -> timeOffPolicyService.getAllPolicyUsersByPolicyId("1"));
  }

  @Test
  void testEnrollTimeOffHours() {
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    final List<TimeOffPolicyUser> timeOffPolicyUsers = new ArrayList<>();
    final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
    timeOffPolicy.setName("007");
    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setBalance(100);

    final User user = new User();
    user.setId("1");
    user.setCompany(new Company("1"));
    timeOffPolicyUser.setUser(user);
    timeOffPolicyUser.setTimeOffPolicy(timeOffPolicy);

    timeOffPolicyUsers.add(timeOffPolicyUser);

    Mockito.when(
            timeOffPolicyUserRepository.findTimeOffPolicyUserByUserAndTimeOffPolicy(
                Mockito.any(), Mockito.any()))
        .thenReturn(null);
    Mockito.when(timeOffPolicyUserRepository.save(Mockito.any())).thenReturn(timeOffPolicyUser);
    Mockito.when(timeOffPolicyUserRepository.findAllByTimeOffPolicyId(Mockito.any()))
        .thenReturn(timeOffPolicyUsers);
    Mockito.when(timeOffPolicyRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(timeOffPolicy));
    Mockito.when(timeOffDetailService.getTimeOffBreakdown(Mockito.any(), Mockito.any()))
        .thenReturn(timeOffBreakdownDto);

    Assertions.assertDoesNotThrow(() -> timeOffPolicyService.enrollTimeOffHours("1", "1", "1"));
  }

  @Test
  void testCheckHasTimeOffPolicies() {
    final boolean bol = timeOffPolicyService.checkHasTimeOffPolicies("1");
    Assertions.assertFalse(bol);
  }

  @Test
  void createAccrualScheduleMilestones() {
    final List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList = new ArrayList<>();
    final AccrualScheduleMilestoneDto accrualScheduleMilestoneDto =
        new AccrualScheduleMilestoneDto();
    accrualScheduleMilestoneDtoList.add(accrualScheduleMilestoneDto);

    final List<AccrualScheduleMilestone> accrualScheduleMilestoneList = new ArrayList<>();
    final AccrualScheduleMilestone accrualScheduleMilestone = new AccrualScheduleMilestone();

    Mockito.when(
            accrualScheduleMilestoneMapper
                .createFromAccrualScheduleMilestoneDtoAndTimeOffPolicyAccrualScheduleId(
                    Mockito.any(), Mockito.any()))
        .thenReturn(accrualScheduleMilestone);
    Mockito.when(accrualScheduleMilestoneRepository.saveAll(Mockito.any()))
        .thenReturn(accrualScheduleMilestoneList);

    Assertions.assertDoesNotThrow(
        () -> {
          Whitebox.invokeMethod(
              timeOffPolicyService,
              "createAccrualScheduleMilestones",
              accrualScheduleMilestoneDtoList,
              "1");
        });
  }

  @Test
  void testAddUserToAutoEnrolledPolicy() {
    final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
    timeOffPolicy.setId("1");
    timeOffPolicy.setIsLimited(true);
    final List<TimeOffPolicy> timeOffPolicyList = new ArrayList<>();
    timeOffPolicyList.add(timeOffPolicy);
    Mockito.when(timeOffPolicyRepository.findByCompanyIdAndIsAutoEnrollEnabledIsTrue(Mockito.any()))
        .thenReturn(timeOffPolicyList);

    Assertions.assertDoesNotThrow(() -> timeOffPolicyService.addUserToAutoEnrolledPolicy("1", "1"));
  }

  @Nested
  class createTimeOffPolicy {
    TimeOffPolicyWrapperDto timeOffPolicyWrapperDto;
    TimeOffPolicyFrontendDto timeOffPolicyFrontendDto;
    TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto;
    List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList;
    List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtoList;
    TimeOffPolicyUserFrontendDto timeOffPolicyUserFrontendDto;
    Company company;
    TimeOffPolicy timeOffPolicy;

    @BeforeEach
    void setUp() {
      timeOffPolicyWrapperDto = new TimeOffPolicyWrapperDto();
      timeOffPolicyFrontendDto = new TimeOffPolicyFrontendDto();
      timeOffPolicyWrapperDto.setTimeOffPolicy(timeOffPolicyFrontendDto);
      timeOffPolicyAccrualScheduleDto = new TimeOffPolicyAccrualScheduleDto();
      timeOffPolicyFrontendDto.setIsLimited(false);
      timeOffPolicyAccrualScheduleDto = new TimeOffPolicyAccrualScheduleDto();
      timeOffPolicyWrapperDto.setTimeOffPolicyAccrualSchedule(timeOffPolicyAccrualScheduleDto);
      accrualScheduleMilestoneDtoList = new ArrayList<>();
      timeOffPolicyWrapperDto.setMilestones(accrualScheduleMilestoneDtoList);
      timeOffPolicyUserFrontendDtoList = new ArrayList<>();
      timeOffPolicyUserFrontendDto = new TimeOffPolicyUserFrontendDto();
      timeOffPolicyUserFrontendDtoList.add(timeOffPolicyUserFrontendDto);
      timeOffPolicyWrapperDto.setUserStartBalances(timeOffPolicyUserFrontendDtoList);
      company = new Company();
      company.setId("1");
      timeOffPolicy = new TimeOffPolicy();
      Mockito.when(timeOffPolicyRepository.save(Mockito.any())).thenReturn(timeOffPolicy);
      Mockito.when(companyService.findById(Mockito.any())).thenReturn(company);
    }

    @Test
    void whenLimitedIsFalseAndCreateTimeOffPolicy_thenShouldSuccess() {
      timeOffPolicyFrontendDto.setIsLimited(false);

      Assertions.assertDoesNotThrow(
          () -> timeOffPolicyService.createTimeOffPolicy(timeOffPolicyWrapperDto, company.getId()));
    }

    @Test
    void whenLimitedIsTrueAndCreateTimeOffPolicy_thenShouldSuccess() {
      timeOffPolicyFrontendDto.setIsLimited(true);

      final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
          new TimeOffPolicyAccrualSchedule();
      timeOffPolicyAccrualSchedule.setId("1");
      Mockito.when(timeOffPolicyAccrualScheduleRepository.save(Mockito.any()))
          .thenReturn(timeOffPolicyAccrualSchedule);

      Assertions.assertDoesNotThrow(
          () -> timeOffPolicyService.createTimeOffPolicy(timeOffPolicyWrapperDto, company.getId()));
    }
  }

  @Nested
  class checkPolicyNameIsExists {

    TimeOffPolicyFrontendDto timeOffPolicy;

    Company company;

    @BeforeEach
    void setUp() {
      timeOffPolicy = new TimeOffPolicyFrontendDto();
      timeOffPolicy.setPolicyName("example");
      timeOffPolicy.setIsLimited(false);
      company = new Company();
      company.setId("1");
    }

    @Test
    void whenExistSamePolicyNameMoreThanZero_thenShouldSuccess() {

      Mockito.when(
              timeOffPolicyRepository.findByPolicyNameAndCompanyId(
                  timeOffPolicy.getPolicyName(), company.getId()))
          .thenReturn(0);

      Assertions.assertDoesNotThrow(
          () -> {
            Whitebox.invokeMethod(
                timeOffPolicyService, "checkPolicyNameIsExists", timeOffPolicy, company.getId(), 0);
          });
    }

    @Test
    void whenExistSamePolicyNameLessThanZero_thenShouldThrow() {

      Mockito.when(
              timeOffPolicyRepository.findByPolicyNameAndCompanyId(
                  timeOffPolicy.getPolicyName(), company.getId()))
          .thenReturn(1);

      Assertions.assertThrows(
          ForbiddenException.class,
          () ->
              Whitebox.invokeMethod(
                  timeOffPolicyService,
                  "checkPolicyNameIsExists",
                  timeOffPolicy,
                  company.getId(),
                  0));
    }
  }

  @Nested
  class GetTimeOffRelatedInfo {
    TimeOffPolicy timeOffPolicy;

    TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule;

    List<AccrualScheduleMilestone> accrualScheduleMilestones;

    @BeforeEach
    void setUp() {
      timeOffPolicy = new TimeOffPolicy();
      timeOffPolicyAccrualSchedule = new TimeOffPolicyAccrualSchedule();
      timeOffPolicyAccrualSchedule.setId("1");
      accrualScheduleMilestones = new LinkedList<>();
      Mockito.when(timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(Mockito.any()))
          .thenReturn(timeOffPolicyAccrualSchedule);
      Mockito.when(timeOffPolicyRepository.findById(Mockito.anyString()))
          .thenReturn(java.util.Optional.ofNullable(timeOffPolicy));
    }

    @Test
    void whenTimeOffPolicyIsTrue() {
      timeOffPolicy.setIsLimited(false);
      timeOffPolicyService.getTimeOffRelatedInfo("1");
      Mockito.verify(timeOffPolicyMapper, Mockito.times(0))
          .createFromTimeOffPolicyAndTimeOffPolicyAccrualScheduleAndAccrualScheduleMilestones(
              Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void whenTimeOffPolicyIsFalse() {
      timeOffPolicy.setIsLimited(true);
      timeOffPolicyService.getTimeOffRelatedInfo("1");
      Mockito.verify(timeOffPolicyMapper, Mockito.times(1))
          .createFromTimeOffPolicyAndTimeOffPolicyAccrualScheduleAndAccrualScheduleMilestones(
              Mockito.any(), Mockito.any(), Mockito.any());
    }
  }

  @Nested
  class updateTimeOffPolicy {
    TimeOffPolicy timeOffPolicy;
    TimeOffPolicyWrapperDto infoWrapper;
    TimeOffPolicyFrontendDto timeOffPolicyFrontendDto;
    List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtoList;
    TimeOffPolicyUserFrontendDto timeOffPolicyUserFrontendDto;
    List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList;
    AccrualScheduleMilestoneDto accrualScheduleMilestoneDto;
    TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule;
    TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule1;

    @BeforeEach
    void init() {
      timeOffPolicy = new TimeOffPolicy();
      timeOffPolicy.setId("1");
      timeOffPolicy.setIsLimited(true);

      infoWrapper = new TimeOffPolicyWrapperDto();
      timeOffPolicyFrontendDto = new TimeOffPolicyFrontendDto();
      infoWrapper.setTimeOffPolicy(timeOffPolicyFrontendDto);

      timeOffPolicyUserFrontendDtoList = new ArrayList<>();
      timeOffPolicyUserFrontendDto = new TimeOffPolicyUserFrontendDto();
      timeOffPolicyUserFrontendDto.setUserId("1");

      infoWrapper.setUserStartBalances(timeOffPolicyUserFrontendDtoList);
      infoWrapper.setTimeOffPolicyAccrualSchedule(new TimeOffPolicyAccrualScheduleDto());

      accrualScheduleMilestoneDtoList = new ArrayList<>();
      infoWrapper.setMilestones(accrualScheduleMilestoneDtoList);

      timeOffPolicyAccrualSchedule = new TimeOffPolicyAccrualSchedule();
      timeOffPolicyAccrualSchedule.setId("1");
      timeOffPolicyAccrualSchedule.setTimeOffAccrualFrequency(new TimeOffAccrualFrequency("1"));
      timeOffPolicyAccrualSchedule.setDaysBeforeAccrualStarts(10);
      timeOffPolicyAccrualSchedule.setAccrualHours(100);
      timeOffPolicyAccrualSchedule.setCarryoverLimit(1);
      timeOffPolicyAccrualSchedule.setMaxBalance(101);

      timeOffPolicyAccrualSchedule1 = new TimeOffPolicyAccrualSchedule();
      timeOffPolicyAccrualSchedule1.setId("1");
      timeOffPolicyAccrualSchedule1.setTimeOffAccrualFrequency(new TimeOffAccrualFrequency("1"));
      timeOffPolicyAccrualSchedule1.setDaysBeforeAccrualStarts(10);
      timeOffPolicyAccrualSchedule1.setAccrualHours(100);
      timeOffPolicyAccrualSchedule1.setCarryoverLimit(1);
      timeOffPolicyAccrualSchedule1.setMaxBalance(102);
    }

    @Test
    void whenOriginTimeOffScheduleIsNull_thenReturnList() {

      Mockito.when(timeOffPolicyRepository.findById(Mockito.any()))
          .thenReturn(Optional.of(timeOffPolicy));
      Mockito.when(timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(Mockito.any()))
          .thenReturn(timeOffPolicyAccrualSchedule);
      Mockito.when(
              timeOffPolicyAccrualScheduleMapper.createTimeOffPolicyAccrualSchedule(
                  Mockito.any(), Mockito.any(), Mockito.any()))
          .thenReturn(timeOffPolicyAccrualSchedule1);
      Mockito.when(timeOffPolicyAccrualScheduleRepository.save(Mockito.any()))
          .thenReturn(timeOffPolicyAccrualSchedule1);

      Assertions.assertDoesNotThrow(
          () ->
              timeOffPolicyService.updateTimeOffPolicy("1", infoWrapper, new Company("1").getId()));
    }

    @Test
    void testUpdateTimeOffPolicy() {
      final List<AccrualScheduleMilestone> accrualScheduleMilestones = new ArrayList<>();
      final AccrualScheduleMilestone accrualScheduleMilestone = new AccrualScheduleMilestone();
      accrualScheduleMilestone.setId("1");
      accrualScheduleMilestone.setAnniversaryYear(2020);
      accrualScheduleMilestone.setAccrualHours(120);
      accrualScheduleMilestone.setCarryoverLimit(10);
      accrualScheduleMilestone.setMaxBalance(100);
      accrualScheduleMilestones.add(accrualScheduleMilestone);

      accrualScheduleMilestoneDto = new AccrualScheduleMilestoneDto();
      accrualScheduleMilestoneDto.setName("007");
      accrualScheduleMilestoneDtoList.add(accrualScheduleMilestoneDto);

      Mockito.when(timeOffPolicyRepository.findById(Mockito.any()))
          .thenReturn(Optional.of(timeOffPolicy));
      Mockito.when(timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(Mockito.any()))
          .thenReturn(timeOffPolicyAccrualSchedule);
      Mockito.when(
              accrualScheduleMilestoneRepository.findByTimeOffPolicyAccrualScheduleId(
                  Mockito.any()))
          .thenReturn(accrualScheduleMilestones);
      Mockito.when(
              accrualScheduleMilestoneMapper
                  .createFromAccrualScheduleMilestoneDtoAndTimeOffPolicyAccrualScheduleId(
                      Mockito.any(), Mockito.any()))
          .thenReturn(accrualScheduleMilestone);
      Mockito.when(
              timeOffPolicyAccrualScheduleMapper.createTimeOffPolicyAccrualSchedule(
                  Mockito.any(), Mockito.any(), Mockito.any()))
          .thenReturn(timeOffPolicyAccrualSchedule1);
      Mockito.when(timeOffPolicyAccrualScheduleRepository.save(Mockito.any()))
          .thenReturn(timeOffPolicyAccrualSchedule1);

      Assertions.assertDoesNotThrow(
          () ->
              timeOffPolicyService.updateTimeOffPolicy("1", infoWrapper, new Company("1").getId()));
    }
  }

  @Nested
  class expireAndSaveMilestones {
    List<AccrualScheduleMilestone> accrualScheduleMilestoneList;

    @BeforeEach
    void init() {
      accrualScheduleMilestoneList = new ArrayList<>();
    }

    @Test
    void whenSizeIsOne_thenShouldSuccess() {
      final AccrualScheduleMilestone accrualScheduleMilestone = new AccrualScheduleMilestone();
      accrualScheduleMilestoneList.add(accrualScheduleMilestone);

      Mockito.when(accrualScheduleMilestoneRepository.save(Mockito.any()))
          .thenReturn(accrualScheduleMilestone);

      Assertions.assertDoesNotThrow(
          () -> {
            Whitebox.invokeMethod(
                timeOffPolicyService, "expireAndSaveMilestones", accrualScheduleMilestoneList);
          });
    }

    @Test
    void whenSizeIsOneAndIdIsNotNull_thenShouldReturnNull() throws Exception {
      final AccrualScheduleMilestone accrualScheduleMilestone = new AccrualScheduleMilestone();
      accrualScheduleMilestone.setId("1");
      accrualScheduleMilestoneList.add(accrualScheduleMilestone);

      Assertions.assertDoesNotThrow(
          () -> {
            Whitebox.invokeMethod(
                timeOffPolicyService, "expireAndSaveMilestones", accrualScheduleMilestoneList);
          });
      Assertions.assertNull(
          Whitebox.invokeMethod(
              timeOffPolicyService, "expireAndSaveMilestones", accrualScheduleMilestoneList));
    }

    @Test
    void whenSizeIsTwo_thenShouldSuccess() {
      final AccrualScheduleMilestone accrualScheduleMilestone = new AccrualScheduleMilestone();
      final AccrualScheduleMilestone accrualScheduleMilestone1 = new AccrualScheduleMilestone();

      accrualScheduleMilestone.setAccrualHours(10);
      accrualScheduleMilestone1.setAccrualHours(10);
      accrualScheduleMilestone.setCarryoverLimit(10);
      accrualScheduleMilestone1.setCarryoverLimit(10);
      accrualScheduleMilestone.setMaxBalance(10);
      accrualScheduleMilestone1.setMaxBalance(100);

      accrualScheduleMilestoneList.add(accrualScheduleMilestone);
      accrualScheduleMilestoneList.add(accrualScheduleMilestone1);

      Mockito.when(accrualScheduleMilestoneRepository.save(Mockito.any()))
          .thenReturn(accrualScheduleMilestone);

      Assertions.assertDoesNotThrow(
          () -> {
            Whitebox.invokeMethod(
                timeOffPolicyService, "expireAndSaveMilestones", accrualScheduleMilestoneList);
          });
    }
  }
}
