package shamu.company.benefit;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.benefit.dto.*;
import shamu.company.benefit.entity.*;
import shamu.company.benefit.dto.BenefitPlanDependentUserDto;
import shamu.company.benefit.dto.BenefitReportParamDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanCoverage;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.benefit.entity.mapper.*;
import shamu.company.benefit.repository.*;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.user.entity.User;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserBenefitsSettingService;

class BenefitPlanServiceTests {

  @InjectMocks private BenefitPlanService benefitPlanService;

  @Mock private UserBenefitsSettingService userBenefitsSettingService;

  @Mock private BenefitPlanUserRepository benefitPlanUserRepository;

  @Mock private MyBenefitsMapper myBenefitsMapper;

  @Mock private BenefitPlanRepository benefitPlanRepository;

  @Mock private BenefitPlanCoverageRepository benefitPlanCoverageRepository;

  @Mock private UserRepository userRepository;

  @Mock private RetirementPlanTypeRepository retirementPlanTypeRepository;

  @Mock private BenefitCoveragesRepository benefitCoveragesRepository;

  @Mock private BenefitPlanMapper benefitPlanMapper;

  @Mock private AwsHelper awsHelper;

  @Mock private BenefitCoveragesMapper benefitCoveragesMapper;

  @Mock private BenefitPlanCoverageMapper benefitPlanCoverageMapper;

  @Mock private BenefitPlanUserMapper benefitPlanUserMapper;

  @Mock private UserMapper userMapper;
  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class getBenefitSummary {
    String userId = "a";
    Long benefitNumber = 1L;
    BigDecimal benefitCost = BigDecimal.valueOf(0);
    List<BenefitPlanUser> benefitPlanUsers;
    List<BenefitPlanDependentUserDto> dependentUsers;
    BenefitPlanDependentUserDto benefitPlanDependentUserDto;
    Set<BenefitPlanDependent> benefitPlanDependents;
    BenefitPlanUser benefitPlanUser;
    BenefitPlan benefitPlan;
    BenefitPlanType.PlanType retirementType = BenefitPlanType.PlanType.RETIREMENT;
    BenefitPlanType.PlanType medicalType = BenefitPlanType.PlanType.MEDICAL;
    BenefitPlanType benefitPlanType;
    BenefitPlanCoverage benefitPlanCoverage;
    BenefitPlanDependent benefitPlanDependent;

    @BeforeEach
    void init() {
      benefitPlanUsers = new ArrayList<>();
      benefitPlanUser = new BenefitPlanUser();
      benefitPlan = new BenefitPlan();
      benefitPlanType = new BenefitPlanType();
      dependentUsers = new ArrayList<>();
      benefitPlanDependentUserDto = new BenefitPlanDependentUserDto();
      benefitPlanCoverage = new BenefitPlanCoverage();
      benefitPlanDependents = new HashSet<>();
      benefitPlanDependent = new BenefitPlanDependent();
    }

    @Test
    void whenBenefitPlanIsRetirement_thenShouldSuccess() {
      benefitPlanUser.setEnrolled(true);
      final Long dependentUsersNum = 0L;
      benefitPlanType.setName(retirementType.getValue());
      benefitPlan.setBenefitPlanType(benefitPlanType);
      benefitPlanUser.setBenefitPlan(benefitPlan);
      benefitPlanUsers.add(benefitPlanUser);
      Mockito.when(benefitPlanUserRepository.findByUserIdAndEnrolledIsTrue(userId))
          .thenReturn(benefitPlanUsers);
      benefitPlanService.getBenefitSummary(userId);
      Mockito.verify(myBenefitsMapper, Mockito.times(1))
          .convertToBenefitSummaryDto(
              benefitNumber, benefitCost, dependentUsersNum, dependentUsers);
    }

    @Test
    void whenBenefitPlanIsNotRetirement_thenShouldSuccess() {
      benefitPlanUser.setEnrolled(true);
      final Long dependentUsersNum = 1L;
      benefitPlanType.setName(medicalType.getValue());
      benefitPlan.setBenefitPlanType(benefitPlanType);
      benefitPlanUser.setBenefitPlan(benefitPlan);
      benefitPlanCoverage.setEmployeeCost(BigDecimal.valueOf(0));
      benefitPlanUser.setBenefitPlanCoverage(benefitPlanCoverage);
      benefitPlanDependent.setId("a");
      benefitPlanDependents.add(benefitPlanDependent);
      benefitPlanUser.setBenefitPlanDependents(benefitPlanDependents);
      benefitPlanUsers.add(benefitPlanUser);
      benefitPlanDependentUserDto.setId("a");
      dependentUsers.add(benefitPlanDependentUserDto);
      Mockito.when(benefitPlanUserRepository.findByUserIdAndEnrolledIsTrue(userId))
          .thenReturn(benefitPlanUsers);
      benefitPlanService.getBenefitSummary(userId);
      Mockito.verify(myBenefitsMapper, Mockito.times(1))
          .convertToBenefitSummaryDto(
              benefitNumber, benefitCost, dependentUsersNum, dependentUsers);
    }
  }

  @Nested
  class getBenefitPlanReportSummary {
    List<String> planIds;
    BenefitReportParamDto benefitReportParamDto;

    @BeforeEach
    void init() {
      planIds = new ArrayList<>();
      benefitReportParamDto = new BenefitReportParamDto();
    }

    @Test
    void whenBenefitPlanIdsIsEmpty_thenShouldSuccess() {
      benefitPlanService.getBenefitPlanReportSummary(benefitReportParamDto, planIds);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(0))
          .getEmployeesEnrolledNumber(Mockito.anyList());
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(0))
          .getCompanyCost(Mockito.anyList());
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(0))
          .getEmployeeCost(Mockito.anyList());
    }

    @Test
    void whenBenefitPlanIdsIsNotEmptyAndCoverageIdIsEmpty_thenShouldSuccess() {
      planIds.add("a");
      benefitReportParamDto.setCoverageId("");
      benefitPlanService.getBenefitPlanReportSummary(benefitReportParamDto, planIds);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(1))
          .getEmployeesEnrolledNumber(Mockito.anyList());
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(1))
          .getCompanyCost(Mockito.anyList());
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(1))
          .getEmployeeCost(Mockito.anyList());
    }

    @Test
    void whenBenefitPlanIdsAndCoverageIdIsNotEmpty_thenShouldSuccess() {
      planIds.add("a");
      benefitReportParamDto.setCoverageId("a");
      benefitPlanService.getBenefitPlanReportSummary(benefitReportParamDto, planIds);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(1))
          .getEmployeesEnrolledNumber(Mockito.anyList());
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(1))
          .getCompanyCostByCoverageId(Mockito.anyList(), Mockito.anyString());
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(1))
          .getEmployeeCostByCoverageId(Mockito.anyList(), Mockito.anyString());
    }
  }

  @Nested
  class getEnrollmentBreakdown {
    List<String> planIds;
    BenefitReportParamDto benefitReportParamDto;

    @BeforeEach
    void init() {
      planIds = new ArrayList<>();
      benefitReportParamDto = new BenefitReportParamDto();
    }

    @Test
    void whenCoverageIsEmpty_thenShouldSuccess() {
      benefitReportParamDto.setCoverageId("");
      benefitPlanService.getEnrollmentBreakdown(benefitReportParamDto, planIds);
      Mockito.verify(benefitPlanRepository, Mockito.times(1))
          .getEnrollmentBreakdown(Mockito.anyList());
    }

    @Test
    void whenCoverageIsNotEmpty_thenShouldSuccess() {
      benefitReportParamDto.setCoverageId("a");
      benefitPlanService.getEnrollmentBreakdown(benefitReportParamDto, planIds);
      Mockito.verify(benefitPlanRepository, Mockito.times(1))
          .getEnrollmentBreakdown(Mockito.anyList(), Mockito.anyString());
    }
  }

  @Nested
  class createBenefitPlan {
    String companyId = "a";
    BenefitPlanCreateDto benefitPlan;
    List<BenefitPlanCoverageDto> coverages;
    List<BenefitPlanUserCreateDto> selectedEmployees;
    Boolean forAllEmployees;
    NewBenefitPlanWrapperDto data;
    @BeforeEach
    void init() {
      benefitPlan = new BenefitPlanCreateDto();
      coverages = new ArrayList<>();
      selectedEmployees = new ArrayList<>();
      forAllEmployees = true;
      data = new NewBenefitPlanWrapperDto();
      BenefitPlanCoverageDto benefitPlanCoverageDto = new BenefitPlanCoverageDto();
      benefitPlanCoverageDto.setCoverageName("CoverageName");
      benefitPlanCoverageDto.setId("");
      coverages.add(benefitPlanCoverageDto);
      benefitPlan.setRetirementTypeId("retirementTypeId");
      data.setBenefitPlan(benefitPlan);
      data.setForAllEmployees(forAllEmployees);
      data.setCoverages(coverages);
    }
    @Test
    void whenBenefitPlan_thenShouldSuccess() {
      List<User> users = new ArrayList<>();
      User user = new User();
      user.setId("userId");
      users.add(user);
      BenefitPlan benefitPlanSaved = new BenefitPlan();
      benefitPlanSaved.setId("benefitPlanId");
      BenefitCoverages benefitCoverages = new BenefitCoverages();
      benefitCoverages.setBenefitPlanId("benefitPlanId");
      benefitCoverages.setName("CoverageName");
      benefitCoverages.setId("");

      BenefitPlanCoverage benefitPlanCoverage = new BenefitPlanCoverage();
      benefitPlanCoverage.setBenefitPlanId("benefitPlanId");
      benefitPlanCoverage.setId("PlanCoverageId");
      Mockito.when(benefitCoveragesRepository.save(Mockito.any())).thenReturn(benefitCoverages);
      Mockito.when(benefitPlanMapper.createFromBenefitPlanCreateDto(benefitPlan)).thenReturn(benefitPlanSaved);
      Mockito.when(userRepository.findAllByCompanyId(companyId)).thenReturn(users);
      Mockito.when(benefitPlanRepository.save(benefitPlanSaved)).thenReturn(benefitPlanSaved);
      Mockito.when(benefitCoveragesRepository.save(benefitCoverages)).thenReturn(benefitCoverages);
      Mockito.when(benefitCoveragesRepository.findById("")).thenReturn(Optional.of(benefitCoverages));
      benefitPlanService.createBenefitPlan(data,companyId);
      Mockito.verify(benefitPlanMapper, Mockito.times(1))
          .convertToBenefitPlanDto(Mockito.any());
    }
  }

  @Nested
  class updateBenefitPlan {
    String benefitPlanId = "planId";
    String companyId = "companyId";
    NewBenefitPlanWrapperDto newBenefitPlanWrapperDto;
    BenefitPlanCreateDto benefitPlan;
    List<BenefitPlanCoverageDto> coverages;
    @BeforeEach
    void init(){
      newBenefitPlanWrapperDto = new NewBenefitPlanWrapperDto();
      coverages = new ArrayList<>();
      BenefitPlanCoverageDto existBenefitPlanCoverageDto = new BenefitPlanCoverageDto();
      BenefitPlanCoverageDto benefitPlanCoverageDto = new BenefitPlanCoverageDto();
      existBenefitPlanCoverageDto.setId("coverageId");
      benefitPlanCoverageDto.setId("");
      coverages.add(existBenefitPlanCoverageDto);
      coverages.add(benefitPlanCoverageDto);
      benefitPlan = new BenefitPlanCreateDto();
      benefitPlan.setBenefitPlanTypeId("typeId");
      benefitPlan.setPlanId(benefitPlanId);
      newBenefitPlanWrapperDto.setBenefitPlan(benefitPlan);
      newBenefitPlanWrapperDto.setCoverages(coverages);
      newBenefitPlanWrapperDto.setForAllEmployees(true);
    }

    @Test
    void whenUpdateBenefitPlan_thenShouldSuccess() {
      BenefitPlan benefitPlan = new BenefitPlan();
      benefitPlan.setId(benefitPlanId);
      List<BenefitPlanCoverage> existBenefitPlanCoverages = new ArrayList<>();
      BenefitPlanCoverage existBenefitPlanCoverage = new BenefitPlanCoverage();
      existBenefitPlanCoverage.setId("coverageId");
      existBenefitPlanCoverages.add(existBenefitPlanCoverage);
      List<BenefitCoverages> benefitCoverages = new ArrayList<>();
      BenefitCoverages benefitCoverage = new BenefitCoverages();
      benefitCoverage.setId("coverageId");
      benefitCoverages.add(benefitCoverage);
      existBenefitPlanCoverage.setBenefitCoverage(benefitCoverage);
      BenefitCoverages savedBenefitCoverages = new BenefitCoverages();
      savedBenefitCoverages.setBenefitPlanId(benefitPlanId);
      savedBenefitCoverages.setId("newCoverageId");
      BenefitPlanCoverage savedBenefitPlanCoverage = new BenefitPlanCoverage();
      savedBenefitPlanCoverage.setBenefitPlanId(benefitPlanId);
      savedBenefitPlanCoverage.setBenefitCoverage(savedBenefitCoverages);
      List<BenefitPlanUser> benefitPlanUserList = new ArrayList<>();
      BenefitPlanUser benefitPlanUser = new BenefitPlanUser();
      benefitPlanUser.setId("benefitPlanUserId");
      User user = new User();
      user.setId("userId");
      benefitPlanUser.setUser(user);
      benefitPlanUserList.add(benefitPlanUser);
      List<User> users = new ArrayList<>();
      users.add(user);
      Mockito.when(benefitPlanRepository.findBenefitPlanById(benefitPlanId)).thenReturn(benefitPlan);
      Mockito.when(benefitPlanRepository.save(benefitPlan)).thenReturn(benefitPlan);
      Mockito.when(benefitPlanCoverageRepository.findAllByBenefitPlanId(benefitPlanId)).thenReturn(existBenefitPlanCoverages);
      Mockito.when(benefitCoveragesRepository.findAllByBenefitPlanId(benefitPlanId)).thenReturn(benefitCoverages);
      Mockito.when(benefitCoveragesRepository.save(savedBenefitCoverages)).thenReturn(savedBenefitCoverages);
      Mockito.when(benefitPlanCoverageRepository.save(savedBenefitPlanCoverage)).thenReturn(savedBenefitPlanCoverage);
      Mockito.when(benefitPlanCoverageRepository.findById("coverageId")).thenReturn(Optional.of(existBenefitPlanCoverage));
      Mockito.when(benefitPlanCoverageRepository.save(existBenefitPlanCoverage)).thenReturn(existBenefitPlanCoverage);
      Mockito.when(benefitPlanUserRepository.findAllByBenefitPlan(new BenefitPlan(benefitPlanId))).thenReturn(benefitPlanUserList);
      Mockito.when(benefitPlanCoverageMapper.createFromBenefitPlanCoverageDtoAndPlanCoverage(Mockito.any(),Mockito.any()))
          .thenReturn(existBenefitPlanCoverage);
      Mockito.when(benefitCoveragesMapper.createFromBenefitPlanCoverageDto(Mockito.any())).thenReturn(savedBenefitCoverages);
      Mockito.when(benefitPlanCoverageMapper.createFromBenefitPlanCoverageDtoAndCoverage(
          Mockito.any(), Mockito.any())).thenReturn(savedBenefitPlanCoverage);
      Mockito.when(userRepository.findAllByCompanyId(companyId)).thenReturn(users);
      benefitPlanService.updateBenefitPlan(newBenefitPlanWrapperDto,benefitPlanId,companyId);
      Mockito.verify(benefitPlanMapper, Mockito.times(1))
          .convertToBenefitPlanDto(Mockito.any());
    }
  }

  @Nested
  class getBenefitPlanPreview {
    String companyId = "companyId";
    String planTypeId = "planTypeId";
    String benefitPlanId = "benefitPlanId";
    @Test
    void whenGetBenefitPlanPreview_thenShouldSuccess() {
      List<BenefitPlan> benefitPlans = new ArrayList<>();
      BenefitPlan benefitPlan = new BenefitPlan();
      benefitPlan.setId(benefitPlanId);
      benefitPlan.setBenefitPlanType(new BenefitPlanType(planTypeId));
      benefitPlans.add(benefitPlan);
      Mockito.when(benefitPlanRepository.findByBenefitPlanTypeIdAndCompanyIdOrderByNameAsc(planTypeId,companyId))
          .thenReturn(benefitPlans);
      Mockito.when(benefitPlanUserRepository.getEligibleEmployeeNumber(benefitPlanId)).thenReturn((long) 1);
      Mockito.when(benefitPlanUserRepository.countByBenefitPlanIdAndConfirmedIsTrue(benefitPlanId)).thenReturn((long) 1);
      benefitPlanService.getBenefitPlanPreview(companyId,planTypeId);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(1))
          .countByBenefitPlanIdAndConfirmedIsTrue(Mockito.any());
    }
  }

  @Nested
  class confirmBenefitPlanEnrollment {
    String userId = "userId";
    String companyId = "companyId";
    List<SelectedEnrollmentInfoDto> selectedBenefitPlanInfo;
    @BeforeEach
    void init() {
      selectedBenefitPlanInfo = new ArrayList<>();
      SelectedEnrollmentInfoDto selectedEnrollmentInfoDto = new SelectedEnrollmentInfoDto();
      selectedEnrollmentInfoDto.setId("enrollmentId");
      selectedEnrollmentInfoDto.setPlanId("planId");
      selectedEnrollmentInfoDto.setBenefitPlanType(BenefitPlanType.PlanType.OTHER.getValue());
      selectedBenefitPlanInfo.add(selectedEnrollmentInfoDto);
    }
    @Test
    void whenConfirmBenefitPlanEnrollment_thenShouldSuccess() {
      Mockito.when(benefitPlanUserRepository.findByUserIdAndBenefitPlanId(userId,"planId"))
          .thenReturn(Optional.of(new BenefitPlanUser()));
      Mockito.when(benefitPlanRepository.findBenefitPlanById("planId"))
          .thenReturn(new BenefitPlan("planId"));
      benefitPlanService.confirmBenefitPlanEnrollment(userId,selectedBenefitPlanInfo,companyId);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(2))
          .save(Mockito.any());
    }
  }

  @Nested
  class isConfirmed {
    String userId;
    @BeforeEach
    void init() {
      userId = "userId";
    }
    @Test
    void whenConfirmed_thenShouldSuccess() {
      Mockito.when(benefitPlanUserRepository.findByUserIdAndConfirmedIsTrue(userId))
          .thenReturn(Mockito.anyList());
      Boolean result = benefitPlanService.isConfirmed(userId);
      Assertions.assertFalse(result);
    }
  }

  @Nested
  class findCoveragesByBenefitPlanId {
    List<BenefitCoverages> benefitCoverageList;
    @BeforeEach
    void init() {
      benefitCoverageList = new ArrayList<>();
      BenefitCoverages benefitCoverages = new BenefitCoverages();
      benefitCoverages.setId("benefitCoverageId");
      benefitCoverageList.add(benefitCoverages);
    }
    @Test
    void whenFindCoveragesByBenefitPlanId_thenShouldSuccess() {
      Mockito.when(benefitCoveragesRepository.findAllByBenefitPlanIdIsNull())
          .thenReturn(benefitCoverageList);
      benefitPlanService.findCoveragesByBenefitPlanId();
      Mockito.verify(benefitCoveragesMapper, Mockito.times(1))
          .convertToBenefitCoveragesDto(Mockito.any());
    }
  }

  @Nested
  class getUserBenefitPlans {
    String userId = "userId";
    List<BenefitPlanUser> benefitPlanUserList;
    BenefitPlanUser benefitPlanUser;
    @BeforeEach
    void init() {
      benefitPlanUserList = new ArrayList<>();
      benefitPlanUser = new BenefitPlanUser();
      benefitPlanUser.setId("benefitPlanUserId");
    }
    @Test
    void whenGetUserBenefitPlans_thenShouldSuccess() {
      Mockito.when(benefitPlanUserRepository.findByUserIdAndEnrolledIsTrue(userId)).thenReturn(benefitPlanUserList);
      benefitPlanService.getUserBenefitPlans(userId);
      Mockito.verify(benefitPlanUserMapper, Mockito.times(0))
          .convertFrom(Mockito.any());
    }
  }

  @Nested
  class getBenefitPlanByPlanId {
    BenefitPlanUpdateDto benefitPlanUpdateDto;
    BenefitCoverages benefitCoverages;
    String userId = "userId";
    @BeforeEach
    void init() {
      benefitPlanUpdateDto = new BenefitPlanUpdateDto();
      List<BenefitPlanCoverageDto> benefitPlanCoverageDtoList = new ArrayList<>();
      BenefitPlanCoverageDto benefitPlanCoverageDto = new BenefitPlanCoverageDto();
      benefitPlanCoverageDto.setCoverageId("coverageId");
      benefitPlanCoverageDtoList.add(benefitPlanCoverageDto);
      benefitPlanUpdateDto.setBenefitPlanCoverages(benefitPlanCoverageDtoList);

      benefitCoverages = new BenefitCoverages();
      benefitCoverages.setName("name");
    }
    @Test
    void whenGetBenefitPlanPlanId_thenShouldSuccess() {
      Mockito.when(benefitPlanMapper.convertToOldBenefitPlanDto(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any()))
          .thenReturn(benefitPlanUpdateDto);
      Mockito.when(benefitCoveragesRepository.findById("coverageId")).thenReturn(Optional.of(benefitCoverages));
      benefitPlanService.getBenefitPlanByPlanId(userId);
      Mockito.verify(benefitCoveragesRepository, Mockito.times(1))
          .findById("coverageId");
    }
  }

  @Nested
  class updateBenefitPlanEmployees {
    List<BenefitPlanUserCreateDto> employees;
    String benefitPlanId;
    String companyId;
    @BeforeEach
    void init() {
      employees = new ArrayList<>();
      benefitPlanId = "benefitId";
      companyId = "companyId";
    }
    @Test
    void whenUpdateBenefitPlanEmployees_thenShouldSuccess() {
      BenefitPlanUserCreateDto employee = new BenefitPlanUserCreateDto();
      employee.setId("employeeId");
      employees.add(employee);

      List<BenefitPlanUser> existPlans = new ArrayList<>();
      BenefitPlanUser existPlan = new BenefitPlanUser();
      List<User> userList = new ArrayList<>();
      User user = new User();
      user.setId("userId");
      userList.add(user);
      existPlan.setUser(user);
      existPlans.add(existPlan);
      Mockito.when(benefitPlanUserRepository.findAllByBenefitPlanId(benefitPlanId))
          .thenReturn(existPlans);
      Mockito.when(userRepository.findAllByCompanyId(companyId)).thenReturn(userList);
      Mockito.when(benefitPlanUserRepository.findAllByBenefitPlanId(benefitPlanId))
          .thenReturn(existPlans);
      benefitPlanService.updateBenefitPlanEmployees(employees,benefitPlanId,companyId);
      Mockito.verify(userMapper, Mockito.times(0))
          .covertToBenefitPlanUserDto(Mockito.any());
    }
  }

  @Nested
  class getUserAvailableBenefitPlans {
    String userId;
    List<BenefitPlanUser> benefitPlanUserList;
    @BeforeEach
    void init() {
      userId = "userId";
      benefitPlanUserList = new ArrayList<>();
      BenefitPlanUser benefitPlanUser = new BenefitPlanUser();
      benefitPlanUser.setId("planUserId");
      benefitPlanUserList.add(benefitPlanUser);
    }
    @Test
    void whenGetUserAvailableBenefitPlans_thenShouldSuccess() {
      Mockito.when(benefitPlanUserRepository.findAllByUserId(userId))
          .thenReturn(benefitPlanUserList);
      benefitPlanService.getUserAvailableBenefitPlans(userId);
      Mockito.verify(benefitPlanUserMapper, Mockito.times(1))
          .convertFrom(Mockito.any());
    }
  }

  @Nested
  class getBenefitPlanReport {
    String typeName;
    String companyId;
    BenefitReportParamDto benefitReportParamDto;
    @BeforeEach
    void init() {
      typeName = "typeName";
      companyId = "companyId";
      benefitReportParamDto = new BenefitReportParamDto();
      benefitReportParamDto.setPlanId("");
      benefitReportParamDto.setCoverageId("coverageId");
    }
    @Test
    void whenGetBenefitPlanReport_thenShouldSuccess() {
      List<String> benefitPlanIds = new ArrayList<>();
      benefitPlanIds.add("benefitPlanId");
      List<EnrollmentBreakdownDto> enrollmentBreakdownDtos = new ArrayList<>();
      EnrollmentBreakdownDto enrollmentBreakdownDto = new EnrollmentBreakdownDto();
      enrollmentBreakdownDto.setPlan("plan");
      enrollmentBreakdownDtos.add(enrollmentBreakdownDto);
      Mockito.when(benefitPlanRepository.getBenefitPlanIds(typeName,companyId))
          .thenReturn(benefitPlanIds);
      Mockito.when(benefitPlanRepository.getBenefitPlans(typeName,companyId))
          .thenReturn(new ArrayList<BenefitReportPlansDto>());
      Mockito.when(benefitPlanCoverageRepository.getBenefitReportCoverages(benefitPlanIds))
          .thenReturn(new ArrayList<BenefitReportCoveragesDto>());
      Mockito.when(benefitPlanRepository.getEnrollmentBreakdown(benefitPlanIds,"coverageId"))
          .thenReturn(enrollmentBreakdownDtos);
      BenefitPlanReportDto benefitPlanReportDto = benefitPlanService
          .getBenefitPlanReport(typeName,benefitReportParamDto,companyId);
      String plan = benefitPlanReportDto.getEnrollmentBreakdownDtos().get(0).getPlan();
      Assertions.assertEquals(plan, "plan");
    }
  }
}
