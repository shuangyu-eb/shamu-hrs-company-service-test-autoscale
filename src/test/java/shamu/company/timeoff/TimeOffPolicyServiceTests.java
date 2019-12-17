package shamu.company.timeoff;

import java.util.LinkedList;
import java.util.List;
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
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.timeoff.dto.TimeOffPolicyFrontendDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.mapper.AccrualScheduleMilestoneMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyAccrualScheduleMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyUserMapper;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.timeoff.repository.TimeOffAdjustmentRepository;
import shamu.company.timeoff.repository.TimeOffPolicyAccrualScheduleRepository;
import shamu.company.timeoff.repository.TimeOffPolicyRepository;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.timeoff.service.TimeOffDetailService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserService;

public class TimeOffPolicyServiceTests {

  private static TimeOffPolicyService timeOffPolicyService;

  @Mock
  private TimeOffPolicyRepository timeOffPolicyRepository;

  @Mock
  private TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  @Mock
  private AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  @Mock
  private TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;

  @Mock
  private JobUserRepository jobUserRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TimeOffRequestRepository timeOffRequestRepository;

  @Mock
  private TimeOffAdjustmentRepository timeOffAdjustmentRepository;

  @Mock
  private TimeOffDetailService timeOffDetailService;

  @Mock
  private CompanyService companyService;

  @Mock
  private AccrualScheduleMilestoneMapper accrualScheduleMilestoneMapper;

  @Mock
  private TimeOffPolicyAccrualScheduleMapper timeOffPolicyAccrualScheduleMapper;

  @Mock
  private TimeOffPolicyUserMapper timeOffPolicyUserMapper;

  @Mock
  private TimeOffPolicyMapper timeOffPolicyMapper;

  @Mock
  private JobUserMapper jobUserMapper;

  @Mock
  private UserService userService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    timeOffPolicyService = new TimeOffPolicyService(timeOffPolicyRepository,
    timeOffPolicyUserRepository,
    accrualScheduleMilestoneRepository,
    timeOffPolicyAccrualScheduleRepository,
    jobUserRepository ,
    timeOffDetailService ,
    userRepository ,
    timeOffRequestRepository ,
    timeOffAdjustmentRepository ,
    accrualScheduleMilestoneMapper,
    timeOffPolicyAccrualScheduleMapper,
    timeOffPolicyUserMapper,
    timeOffPolicyMapper ,
    jobUserMapper ,
    companyService,
    userService);

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

      Mockito.when(timeOffPolicyRepository.findByPolicyNameAndCompanyId(timeOffPolicy.getPolicyName(),company.getId()))
          .thenReturn(0);

      Assertions.assertDoesNotThrow(() -> {
        Whitebox.invokeMethod(timeOffPolicyService,"checkPolicyNameIsExists",timeOffPolicy,company.getId(),0);
      });
    }


    @Test
    void whenExistSamePolicyNameLessThanZero_thenShouldThrow() {

      Mockito.when(timeOffPolicyRepository.findByPolicyNameAndCompanyId(timeOffPolicy.getPolicyName(),company.getId()))
          .thenReturn(1);

      Assertions.assertThrows(ForbiddenException.class, () -> {
        Whitebox.invokeMethod(timeOffPolicyService,"checkPolicyNameIsExists",timeOffPolicy,company.getId(),0);
      });

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
      timeOffPolicyAccrualSchedule= new TimeOffPolicyAccrualSchedule();
      timeOffPolicyAccrualSchedule.setId("1");
      accrualScheduleMilestones = new LinkedList<AccrualScheduleMilestone>();
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
          .createFromTimeOffPolicyAndTimeOffPolicyAccrualScheduleAndAccrualScheduleMilestones(Mockito.any(),Mockito.any(),Mockito.any());
    }

    @Test
    void whenTimeOffPolicyIsFalse() {
      timeOffPolicy.setIsLimited(true);
      timeOffPolicyService.getTimeOffRelatedInfo("1");
      Mockito.verify(timeOffPolicyMapper, Mockito.times(1))
          .createFromTimeOffPolicyAndTimeOffPolicyAccrualScheduleAndAccrualScheduleMilestones(Mockito.any(),Mockito.any(),Mockito.any());

    }


  }




}
