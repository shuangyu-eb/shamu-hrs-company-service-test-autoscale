package shamu.company.benefit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import shamu.company.benefit.dto.BenefitPlanCoverageDto;
import shamu.company.benefit.dto.BenefitPlanCreateDto;
import shamu.company.benefit.dto.BenefitPlanDependentUserDto;
import shamu.company.benefit.dto.BenefitPlanDto;
import shamu.company.benefit.dto.BenefitPlanReportDto;
import shamu.company.benefit.dto.BenefitPlanSearchCondition;
import shamu.company.benefit.dto.BenefitPlanUpdateDto;
import shamu.company.benefit.dto.BenefitPlanUserCreateDto;
import shamu.company.benefit.dto.BenefitPlanUserDto;
import shamu.company.benefit.dto.BenefitReportCoveragesDto;
import shamu.company.benefit.dto.BenefitReportParamDto;
import shamu.company.benefit.dto.EnrollmentBreakdownDto;
import shamu.company.benefit.dto.EnrollmentBreakdownSearchCondition;
import shamu.company.benefit.dto.NewBenefitPlanWrapperDto;
import shamu.company.benefit.dto.SelectedEnrollmentInfoDto;
import shamu.company.benefit.entity.BenefitCoverages;
import shamu.company.benefit.entity.BenefitDependentRecord;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanCoverage;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.BenefitPlanPreviewPojo;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.benefit.entity.BenefitReportPlansPojo;
import shamu.company.benefit.entity.EnrollmentBreakdownPojo;
import shamu.company.benefit.entity.mapper.BenefitCoveragesMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanCoverageMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanDependentMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanReportMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanUserMapper;
import shamu.company.benefit.entity.mapper.MyBenefitsMapper;
import shamu.company.benefit.repository.BenefitCoveragesRepository;
import shamu.company.benefit.repository.BenefitPlanCoverageRepository;
import shamu.company.benefit.repository.BenefitPlanDependentRepository;
import shamu.company.benefit.repository.BenefitPlanRepository;
import shamu.company.benefit.repository.BenefitPlanTypeRepository;
import shamu.company.benefit.repository.BenefitPlanUserRepository;
import shamu.company.benefit.repository.RetirementPlanTypeRepository;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.service.JobUserService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserBenefitsSettingService;

class BenefitPlanServiceTests {

  @InjectMocks private BenefitPlanService benefitPlanService;

  @Mock private UserBenefitsSettingService userBenefitsSettingService;

  @Mock private JobUserService jobUserService;

  @Mock private BenefitPlanTypeRepository benefitPlanTypeRepository;

  @Mock private BenefitPlanDependentRepository benefitPlanDependentRepository;

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

  @Mock private JobUserMapper jobUserMapper;

  @Mock private BenefitPlanReportMapper benefitPlanReportMapper;

  @Mock private BenefitPlanDependentMapper benefitPlanDependentMapper;

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
              Mockito.anyLong(), Mockito.any(), Mockito.anyLong(), Mockito.anyList());
    }
  }

  @Nested
  class findBenefitPlanReportSummary {
    List<String> planIds;
    BenefitReportParamDto benefitReportParamDto;

    @BeforeEach
    void init() {
      planIds = new ArrayList<>();
      benefitReportParamDto = new BenefitReportParamDto();
    }

    @Test
    void whenBenefitPlanIdsIsEmpty_thenShouldSuccess() {
      benefitPlanService.findBenefitPlanReportSummary(benefitReportParamDto, planIds);
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
      benefitPlanService.findBenefitPlanReportSummary(benefitReportParamDto, planIds);
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
      benefitPlanService.findBenefitPlanReportSummary(benefitReportParamDto, planIds);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(1))
          .getEmployeesEnrolledNumberByCoverageId(Mockito.anyList(), Mockito.anyString());
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(1))
          .getCompanyCostByCoverageId(Mockito.anyList(), Mockito.anyString());
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(1))
          .getEmployeeCostByCoverageId(Mockito.anyList(), Mockito.anyString());
    }
  }

  @Nested
  class findEnrollmentBreakdownToExport {
    List<String> planIds;
    BenefitReportParamDto benefitReportParamDto;

    @BeforeEach
    void init() {
      planIds = new ArrayList<>();
      benefitReportParamDto = new BenefitReportParamDto();
    }

    @Test
    void whenCoverageIsEmpty_thenShouldSuccess() {
      planIds.add("a");
      benefitReportParamDto.setCoverageId("");
      benefitPlanService.findEnrollmentBreakdownToExport(benefitReportParamDto, planIds);
      Mockito.verify(benefitPlanRepository, Mockito.times(1))
          .getEnrollmentBreakdown(Mockito.anyList());
    }

    @Test
    void whenCoverageIsNotEmpty_thenShouldSuccess() {
      planIds.add("a");
      benefitReportParamDto.setCoverageId("a");
      benefitPlanService.findEnrollmentBreakdownToExport(benefitReportParamDto, planIds);
      Mockito.verify(benefitPlanRepository, Mockito.times(1))
          .getEnrollmentBreakdown(Mockito.anyList(), Mockito.anyList());
    }
  }

  @Nested
  class findBenefitCoveragesByPlanIds {
    List<String> benefitPlanIds;

    @BeforeEach
    void init() {
      benefitPlanIds = new ArrayList<>();
    }

    @Test
    void whenBenefitPlanIdsIsNotEmpty_thenShouldSuccess() {
      benefitPlanIds.add("a");
      benefitPlanService.findBenefitCoveragesByPlanIds(benefitPlanIds);
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(1))
          .getBenefitReportCoverages(benefitPlanIds);
    }

    @Test
    void whenBenefitPlanIdsIsEmpty_thenShouldSuccess() {
      benefitPlanService.findBenefitCoveragesByPlanIds(benefitPlanIds);
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(0))
          .getBenefitReportCoverages(benefitPlanIds);
    }
  }

  @Nested
  class findEnrollmentBreakdown {
    Pageable paramPageable;
    BenefitReportParamDto benefitReportParamDto;
    List<String> benefitPlanIds;
    EnrollmentBreakdownSearchCondition enrollmentBreakdownSearchCondition;
    EnrollmentBreakdownDto enrollmentBreakdownDto;
    Page<EnrollmentBreakdownPojo> enrollmentBreakdownDtoPage;
    List<EnrollmentBreakdownPojo> enrollmentBreakdownPojos;
    String planTypeName = "medical";
    String companyId = "a";
    EnrollmentBreakdownPojo enrollmentBreakdownPojo =
        new EnrollmentBreakdownPojo() {
          @Override
          public String getPlanUserId() {
            return "a";
          }

          @Override
          public long getNumber() {
            return 0;
          }

          @Override
          public String getImageUrl() {
            return "a";
          }

          @Override
          public String getFullName() {
            return "a";
          }

          @Override
          public String getOrderName() {
            return "a";
          }

          @Override
          public String getPlan() {
            return "a";
          }

          @Override
          public String getCoverage() {
            return "a";
          }

          @Override
          public BigDecimal getCompanyCost() {
            return BigDecimal.valueOf(1);
          }

          @Override
          public BigDecimal getEmployeeCost() {
            return BigDecimal.valueOf(1);
          }

          @Override
          public List<String> dependentUsername() {
            return Collections.singletonList("a");
          }
        };

    @BeforeEach
    void init() {
      enrollmentBreakdownDto = new EnrollmentBreakdownDto();
      enrollmentBreakdownPojos = new ArrayList<>();
      enrollmentBreakdownPojos.add(enrollmentBreakdownPojo);
      enrollmentBreakdownSearchCondition = new EnrollmentBreakdownSearchCondition();
      enrollmentBreakdownSearchCondition.setPage(0);
      enrollmentBreakdownSearchCondition.setSize(20);
      enrollmentBreakdownSearchCondition.setSortDirection(
          EnrollmentBreakdownSearchCondition.SortDirection.ASC.name());
      enrollmentBreakdownSearchCondition.setSortField(
          EnrollmentBreakdownSearchCondition.SortField.NAME.name());
      paramPageable =
          PageRequest.of(
              enrollmentBreakdownSearchCondition.getPage(),
              enrollmentBreakdownSearchCondition.getSize(),
              Sort.Direction.valueOf(
                  enrollmentBreakdownSearchCondition.getSortDirection().toUpperCase()),
              enrollmentBreakdownSearchCondition.getSortField().getSortValue());
      benefitReportParamDto = new BenefitReportParamDto();
      benefitPlanIds = new ArrayList<>();
      benefitPlanIds.add("planId");
    }

    @Test
    void whenBenefitPlanIdIsEmpty_thenShouldSuccess() {
      final BenefitCoverages benefitCoverages = new BenefitCoverages();
      benefitCoverages.setName("name");
      final List<String> coverageIds = new ArrayList<>();
      coverageIds.add("coverageId");
      benefitReportParamDto.setPlanId("active");
      enrollmentBreakdownSearchCondition.setCoverageId("coverageId");
      enrollmentBreakdownDtoPage = new PageImpl<>(enrollmentBreakdownPojos, paramPageable, 0);

      Mockito.when(benefitPlanRepository.getActiveBenefitPlanIds(planTypeName, companyId))
          .thenReturn(benefitPlanIds);
      Mockito.when(benefitCoveragesRepository.findById("coverageId"))
          .thenReturn(Optional.of(benefitCoverages));
      Mockito.when(benefitCoveragesRepository.getCoverageIdsByNameAndPlan("name", benefitPlanIds))
          .thenReturn(coverageIds);
      Mockito.when(
              benefitPlanRepository.getEnrollmentBreakdownByConditionAndCoverageId(
                  benefitPlanIds, coverageIds, paramPageable))
          .thenReturn(enrollmentBreakdownDtoPage);

      Mockito.when(
              benefitPlanRepository.getEnrollmentBreakdownByConditionAndPlanIdIsEmpty(
                  benefitPlanIds, companyId, paramPageable))
          .thenReturn(enrollmentBreakdownDtoPage);
      Mockito.when(benefitPlanRepository.getActiveBenefitPlanIds(planTypeName, companyId))
          .thenReturn(benefitPlanIds);
      benefitPlanService.findEnrollmentBreakdown(
          enrollmentBreakdownSearchCondition, planTypeName, benefitReportParamDto, companyId);
      Mockito.verify(benefitPlanRepository, Mockito.times(1))
          .getEnrollmentBreakdownByConditionAndCoverageId(
              benefitPlanIds, coverageIds, paramPageable);
    }

    @Test
    void whenBenefitPlanIdIsActiveAndCoverageIdIsEmpty_thenShouldSuccess() throws Exception {
      benefitPlanIds.add("active");
      benefitReportParamDto.setCoverageId("");
      benefitReportParamDto.setPlanId("active");
      enrollmentBreakdownDtoPage = new PageImpl<>(enrollmentBreakdownPojos, paramPageable, 0);
      Mockito.when(
              benefitPlanRepository.getEnrollmentBreakdownByCondition(
                  benefitPlanIds, paramPageable))
          .thenReturn(enrollmentBreakdownDtoPage);
      benefitPlanService.findEnrollmentBreakdown(
          enrollmentBreakdownSearchCondition, planTypeName, benefitReportParamDto, companyId);
      Mockito.verify(benefitPlanRepository, Mockito.times(1))
          .getActiveBenefitPlanIds(planTypeName, companyId);
    }

    @Test
    void whenBenefitPlanIdIsExpiredAndCoverageIdIsEmpty_thenShouldSuccess() throws Exception {
      benefitPlanIds.add("expired");
      benefitReportParamDto.setCoverageId("");
      benefitReportParamDto.setPlanId("expired");
      enrollmentBreakdownDtoPage = new PageImpl<>(enrollmentBreakdownPojos, paramPageable, 0);
      Mockito.when(
              benefitPlanRepository.getEnrollmentBreakdownByCondition(
                  benefitPlanIds, paramPageable))
          .thenReturn(enrollmentBreakdownDtoPage);
      benefitPlanService.findEnrollmentBreakdown(
          enrollmentBreakdownSearchCondition, planTypeName, benefitReportParamDto, companyId);
      Mockito.verify(benefitPlanRepository, Mockito.times(1))
          .getExpiredBenefitPlanIds(planTypeName, companyId);
    }

    @Test
    void whenBenefitPlanIdIsStartingAndCoverageIdIsEmpty_thenShouldSuccess() throws Exception {
      benefitPlanIds.add("starting");
      benefitReportParamDto.setCoverageId("");
      benefitReportParamDto.setPlanId("starting");
      enrollmentBreakdownDtoPage = new PageImpl<>(enrollmentBreakdownPojos, paramPageable, 0);
      Mockito.when(
              benefitPlanRepository.getEnrollmentBreakdownByCondition(
                  benefitPlanIds, paramPageable))
          .thenReturn(enrollmentBreakdownDtoPage);
      benefitPlanService.findEnrollmentBreakdown(
          enrollmentBreakdownSearchCondition, planTypeName, benefitReportParamDto, companyId);
      Mockito.verify(benefitPlanRepository, Mockito.times(1))
          .getStartingBenefitPlanIds(planTypeName, companyId);
    }

    @Test
    void whenBenefitPlanIdIsNotEmptyAndCoverageIdIsEmpty_thenShouldSuccess() throws Exception {
      benefitReportParamDto.setCoverageId("");
      benefitReportParamDto.setPlanId("planId");
      enrollmentBreakdownDtoPage = new PageImpl<>(enrollmentBreakdownPojos, paramPageable, 0);
      Mockito.when(
              benefitPlanRepository.getEnrollmentBreakdownByCondition(
                  benefitPlanIds, paramPageable))
          .thenReturn(enrollmentBreakdownDtoPage);
      benefitPlanService.findEnrollmentBreakdown(
          enrollmentBreakdownSearchCondition, planTypeName, benefitReportParamDto, companyId);
      Mockito.verify(benefitPlanRepository, Mockito.times(1))
          .getEnrollmentBreakdownByCondition(benefitPlanIds, paramPageable);
    }

    @Test
    void whenBenefitPlanIdsIsNotEmptyAndCoverageIdIsNotEmpty_thenShouldSuccess() throws Exception {
      benefitPlanIds.add("a");
      benefitReportParamDto.setCoverageId("a");
      enrollmentBreakdownDtoPage = new PageImpl<>(enrollmentBreakdownPojos, paramPageable, 0);
      Mockito.when(
              benefitPlanRepository.getEnrollmentBreakdownByConditionAndCoverageId(
                  benefitPlanIds,
                  Collections.singletonList(benefitReportParamDto.getCoverageId()),
                  paramPageable))
          .thenReturn(enrollmentBreakdownDtoPage);
      Whitebox.invokeMethod(
          benefitPlanService,
          "findEnrollmentBreakdownByCondition",
          paramPageable,
          benefitReportParamDto,
          benefitPlanIds);
      Mockito.verify(benefitPlanRepository, Mockito.times(1))
          .getEnrollmentBreakdownByConditionAndCoverageId(
              benefitPlanIds,
              Collections.singletonList(benefitReportParamDto.getCoverageId()),
              paramPageable);
    }
  }

  @Nested
  class getPageable {
    EnrollmentBreakdownSearchCondition enrollmentBreakdownSearchCondition;
    Pageable paramPageable;

    @BeforeEach
    void init() {
      enrollmentBreakdownSearchCondition = new EnrollmentBreakdownSearchCondition();
      enrollmentBreakdownSearchCondition.setPage(0);
      enrollmentBreakdownSearchCondition.setSize(20);
      enrollmentBreakdownSearchCondition.setSortDirection(
          EnrollmentBreakdownSearchCondition.SortDirection.ASC.name());
      enrollmentBreakdownSearchCondition.setSortField(
          EnrollmentBreakdownSearchCondition.SortField.COVERAGE.name());
    }

    @Test
    void whenSortFieldIsNotName_thenShouldSuccess() throws Exception {
      final String sortDirection =
          enrollmentBreakdownSearchCondition.getSortDirection().toUpperCase();
      final String sortValue = enrollmentBreakdownSearchCondition.getSortField().getSortValue();
      final Sort.Order order = new Sort.Order(Sort.Direction.valueOf(sortDirection), sortValue);
      final Sort.Order orderName =
          new Sort.Order(
              Sort.Direction.ASC, EnrollmentBreakdownSearchCondition.SortField.NAME.getSortValue());
      final Sort sort = Sort.by(order, orderName);
      paramPageable =
          PageRequest.of(
              enrollmentBreakdownSearchCondition.getPage(),
              enrollmentBreakdownSearchCondition.getSize(),
              sort);
      final Pageable pageable =
          Whitebox.invokeMethod(
              benefitPlanService, "getPageable", enrollmentBreakdownSearchCondition);
      assertThat(pageable).isEqualTo(paramPageable);
    }
  }

  @Nested
  class createBenefitPlan {
    String companyId = "companyId";
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
      final BenefitPlanCoverageDto benefitPlanCoverageDto = new BenefitPlanCoverageDto();
      benefitPlanCoverageDto.setCoverageName("CoverageName");
      benefitPlanCoverageDto.setId("coverageId");
      coverages.add(benefitPlanCoverageDto);
      benefitPlan.setRetirementTypeId("retirementTypeId");
      final BenefitPlanUserCreateDto benefitPlanUserCreateDto = new BenefitPlanUserCreateDto();
      benefitPlanUserCreateDto.setCoverage("coverageId");
      benefitPlanUserCreateDto.setId("userId");
      selectedEmployees.add(benefitPlanUserCreateDto);
      data.setSelectedEmployees(selectedEmployees);
      data.setBenefitPlan(benefitPlan);
      data.setCoverages(coverages);
    }

    @Test
    void whenBenefitPlan_thenShouldSuccess() {
      final BenefitPlan benefitPlanSaved = new BenefitPlan();
      benefitPlanSaved.setId("benefitPlanId");
      final BenefitCoverages benefitCoverages = new BenefitCoverages();
      benefitCoverages.setBenefitPlanId("benefitPlanId");
      benefitCoverages.setName("CoverageName");
      benefitCoverages.setId("");

      final BenefitPlanCoverage benefitPlanCoverage = new BenefitPlanCoverage();
      benefitPlanCoverage.setBenefitPlanId("benefitPlanId");
      benefitPlanCoverage.setId("PlanCoverageId");
      Mockito.when(benefitCoveragesRepository.save(Mockito.any())).thenReturn(benefitCoverages);
      Mockito.when(benefitPlanMapper.createFromBenefitPlanCreateDto(benefitPlan))
          .thenReturn(benefitPlanSaved);
      Mockito.when(benefitPlanRepository.save(benefitPlanSaved)).thenReturn(benefitPlanSaved);
      Mockito.when(benefitCoveragesRepository.save(benefitCoverages)).thenReturn(benefitCoverages);
      Mockito.when(
              benefitPlanCoverageMapper.createFromBenefitPlanCoverageAndBenefitPlan(
                  Mockito.any(), Mockito.any(), Mockito.any()))
          .thenReturn(benefitPlanCoverage);
      Mockito.when(benefitCoveragesRepository.findById("coverageId"))
          .thenReturn(Optional.of(benefitCoverages));
      benefitPlanService.createBenefitPlan(data, companyId);
      Mockito.verify(benefitPlanMapper, Mockito.times(1)).convertToBenefitPlanDto(Mockito.any());
    }
  }

  @Nested
  class updateBenefitPlan {
    final BenefitPlanCoverageDto benefitPlanCoverageDto = new BenefitPlanCoverageDto();
    final BenefitPlanCoverage benefitPlanCoverage = new BenefitPlanCoverage();
    String benefitPlanId = "planId";
    String companyId = "companyId";
    NewBenefitPlanWrapperDto newBenefitPlanWrapperDto;
    BenefitPlanCreateDto benefitPlan;
    List<BenefitPlanCoverageDto> coverages;
    List<BenefitPlanUserCreateDto> selectedEmployees;

    @BeforeEach
    void init() {
      newBenefitPlanWrapperDto = new NewBenefitPlanWrapperDto();
      coverages = new ArrayList<>();
      selectedEmployees = new ArrayList<>();
      benefitPlanCoverage.setId("addCoverageId");
      final BenefitPlanUserCreateDto benefitPlanUserCreateDto = new BenefitPlanUserCreateDto();
      benefitPlanUserCreateDto.setId("userId");
      benefitPlanUserCreateDto.setCoverage("coverageId");
      selectedEmployees.add(benefitPlanUserCreateDto);
      newBenefitPlanWrapperDto.setSelectedEmployees(selectedEmployees);
      final BenefitPlanCoverageDto existBenefitPlanCoverageDto = new BenefitPlanCoverageDto();
      existBenefitPlanCoverageDto.setId("coverageId");
      benefitPlanCoverageDto.setId("addCoverageId");
      coverages.add(existBenefitPlanCoverageDto);
      coverages.add(benefitPlanCoverageDto);
      benefitPlan = new BenefitPlanCreateDto();
      benefitPlan.setBenefitPlanTypeId("typeId");
      benefitPlan.setPlanId(benefitPlanId);
      newBenefitPlanWrapperDto.setBenefitPlan(benefitPlan);
      newBenefitPlanWrapperDto.setCoverages(coverages);
    }

    @Test
    void whenUpdateBenefitPlan_thenShouldSuccess() {
      final BenefitPlan benefitPlan = new BenefitPlan();
      benefitPlan.setId(benefitPlanId);
      final BenefitPlanCoverage newBenefitPlanCoverage = new BenefitPlanCoverage();
      newBenefitPlanCoverage.setId("addCoverageId");
      final List<BenefitPlanCoverage> existBenefitPlanCoverages = new ArrayList<>();
      final BenefitPlanCoverage existBenefitPlanCoverage = new BenefitPlanCoverage();
      existBenefitPlanCoverage.setId("coverageId");
      existBenefitPlanCoverages.add(existBenefitPlanCoverage);
      final List<BenefitCoverages> benefitCoverages = new ArrayList<>();
      final BenefitCoverages benefitCoverage = new BenefitCoverages();
      benefitCoverage.setId("coverageId");
      benefitCoverages.add(benefitCoverage);
      existBenefitPlanCoverage.setBenefitCoverage(benefitCoverage);
      final BenefitCoverages savedBenefitCoverages = new BenefitCoverages();
      savedBenefitCoverages.setBenefitPlanId(benefitPlanId);
      savedBenefitCoverages.setId("newCoverageId");
      final BenefitPlanCoverage savedBenefitPlanCoverage = new BenefitPlanCoverage();
      savedBenefitPlanCoverage.setBenefitPlanId(benefitPlanId);
      savedBenefitPlanCoverage.setBenefitCoverage(savedBenefitCoverages);
      savedBenefitPlanCoverage.setId("addCoverageId");
      final List<BenefitPlanUser> benefitPlanUserList = new ArrayList<>();
      final BenefitPlanUser benefitPlanUser = new BenefitPlanUser();
      benefitPlanUser.setId("benefitPlanUserId");
      final User user = new User();
      user.setId("userId");
      benefitPlanUser.setUser(user);
      benefitPlanUserList.add(benefitPlanUser);
      final List<User> users = new ArrayList<>();
      users.add(user);
      Mockito.when(benefitPlanRepository.findBenefitPlanById(benefitPlanId))
          .thenReturn(benefitPlan);
      Mockito.when(benefitPlanRepository.save(benefitPlan)).thenReturn(benefitPlan);
      Mockito.when(benefitPlanCoverageRepository.findAllByBenefitPlanId(benefitPlanId))
          .thenReturn(existBenefitPlanCoverages);
      Mockito.when(benefitCoveragesRepository.findAllByBenefitPlanId(benefitPlanId))
          .thenReturn(benefitCoverages);
      Mockito.when(benefitCoveragesRepository.save(savedBenefitCoverages))
          .thenReturn(savedBenefitCoverages);
      Mockito.when(benefitPlanCoverageRepository.save(savedBenefitPlanCoverage))
          .thenReturn(savedBenefitPlanCoverage);
      Mockito.when(benefitPlanCoverageRepository.findById("coverageId"))
          .thenReturn(Optional.of(existBenefitPlanCoverage));
      Mockito.when(benefitPlanCoverageRepository.save(existBenefitPlanCoverage))
          .thenReturn(existBenefitPlanCoverage);
      Mockito.when(benefitPlanUserRepository.findAllByBenefitPlan(new BenefitPlan(benefitPlanId)))
          .thenReturn(benefitPlanUserList);
      Mockito.when(
              benefitPlanCoverageMapper.createFromBenefitPlanCoverageDtoAndPlanCoverage(
                  Mockito.any(), Mockito.any()))
          .thenReturn(existBenefitPlanCoverage);
      Mockito.when(benefitCoveragesMapper.createFromBenefitPlanCoverageDto(Mockito.any()))
          .thenReturn(savedBenefitCoverages);
      Mockito.when(
              benefitPlanCoverageMapper.createFromBenefitPlanCoverageDtoAndCoverage(
                  Mockito.any(), Mockito.any()))
          .thenReturn(savedBenefitPlanCoverage);
      Mockito.when(benefitPlanCoverageRepository.save(benefitPlanCoverage))
          .thenReturn(benefitPlanCoverage);
      benefitPlanService.updateBenefitPlan(newBenefitPlanWrapperDto, benefitPlanId);
      Mockito.verify(benefitPlanMapper, Mockito.times(1)).convertToBenefitPlanDto(Mockito.any());
    }
  }

  @Nested
  class getBenefitPlanPreview {
    String companyId = "companyId";
    String planTypeId = "planTypeId";
    String benefitPlanId = "benefitPlanId";

    @Test
    void whenGetBenefitPlanPreview_thenShouldSuccess() {
      final List<BenefitPlan> benefitPlans = new ArrayList<>();
      final BenefitPlan benefitPlan = new BenefitPlan();
      benefitPlan.setId(benefitPlanId);
      benefitPlan.setBenefitPlanType(new BenefitPlanType(planTypeId));
      benefitPlans.add(benefitPlan);
      Mockito.when(
              benefitPlanRepository.findByBenefitPlanTypeIdAndCompanyIdOrderByNameAsc(
                  planTypeId, companyId))
          .thenReturn(benefitPlans);
      Mockito.when(benefitPlanUserRepository.getEligibleEmployeeNumber(benefitPlanId))
          .thenReturn((long) 1);
      Mockito.when(benefitPlanUserRepository.countByBenefitPlanIdAndConfirmedIsTrue(benefitPlanId))
          .thenReturn((long) 1);
      benefitPlanService.getBenefitPlanPreview(companyId, planTypeId);
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
      final SelectedEnrollmentInfoDto selectedEnrollmentInfoDto = new SelectedEnrollmentInfoDto();
      selectedEnrollmentInfoDto.setId("enrollmentId");
      selectedEnrollmentInfoDto.setPlanId("planId");
      selectedEnrollmentInfoDto.setBenefitPlanType(BenefitPlanType.PlanType.OTHER.getValue());
      selectedBenefitPlanInfo.add(selectedEnrollmentInfoDto);
    }

    @Test
    void whenConfirmBenefitPlanEnrollment_thenShouldSuccess() {
      Mockito.when(benefitPlanUserRepository.findByUserIdAndBenefitPlanId(userId, "planId"))
          .thenReturn(Optional.of(new BenefitPlanUser()));
      Mockito.when(benefitPlanRepository.findBenefitPlanById("planId"))
          .thenReturn(new BenefitPlan("planId"));
      benefitPlanService.confirmBenefitPlanEnrollment(userId, selectedBenefitPlanInfo, companyId);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(2)).save(Mockito.any());
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
      assertThat(benefitPlanService.isConfirmed(userId)).isFalse();
    }
  }

  @Nested
  class findCoveragesByBenefitPlanId {
    List<BenefitCoverages> benefitCoverageList;

    @BeforeEach
    void init() {
      benefitCoverageList = new ArrayList<>();
      final BenefitCoverages benefitCoverages = new BenefitCoverages();
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
      Mockito.when(benefitPlanUserRepository.findByUserIdAndEnrolledIsTrue(userId))
          .thenReturn(benefitPlanUserList);
      benefitPlanService.getUserBenefitPlans(userId);
      Mockito.verify(benefitPlanUserMapper, Mockito.times(0)).convertFrom(Mockito.any());
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
      final List<BenefitPlanCoverageDto> benefitPlanCoverageDtoList = new ArrayList<>();
      final BenefitPlanCoverageDto benefitPlanCoverageDto = new BenefitPlanCoverageDto();
      benefitPlanCoverageDto.setCoverageId("coverageId");
      benefitPlanCoverageDtoList.add(benefitPlanCoverageDto);
      benefitPlanUpdateDto.setBenefitPlanCoverages(benefitPlanCoverageDtoList);

      benefitCoverages = new BenefitCoverages();
      benefitCoverages.setName("name");
    }

    @Test
    void whenGetBenefitPlanPlanId_thenShouldSuccess() {
      Mockito.when(
              benefitPlanMapper.convertToOldBenefitPlanDto(
                  Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
          .thenReturn(benefitPlanUpdateDto);
      Mockito.when(benefitCoveragesRepository.findById("coverageId"))
          .thenReturn(Optional.of(benefitCoverages));
      benefitPlanService.getBenefitPlanByPlanId(userId);
      Mockito.verify(benefitCoveragesRepository, Mockito.times(1)).findById("coverageId");
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
    }

    @Test
    void whenUpdateBenefitPlanEmployees_thenShouldSuccess() {
      final BenefitPlanUserCreateDto employee = new BenefitPlanUserCreateDto();
      employee.setId("employeeId");
      employee.setCoverage("coverageId");
      employees.add(employee);

      benefitPlanService.updateBenefitPlanEmployees(employees, benefitPlanId);
      Mockito.verify(userMapper, Mockito.times(0)).covertToBenefitPlanUserDto(Mockito.any());
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
      final BenefitPlanUser benefitPlanUser = new BenefitPlanUser();
      benefitPlanUser.setId("planUserId");
      benefitPlanUserList.add(benefitPlanUser);
    }

    @Test
    void whenGetUserAvailableBenefitPlans_thenShouldSuccess() {
      Mockito.when(benefitPlanUserRepository.findAllByUserId(userId))
          .thenReturn(benefitPlanUserList);
      benefitPlanService.getUserAvailableBenefitPlans(userId);
      Mockito.verify(benefitPlanUserMapper, Mockito.times(1)).convertFrom(Mockito.any());
    }
  }

  @Nested
  class findBenefitPlanReport {
    String typeName;
    String companyId;
    BenefitReportParamDto benefitReportParamDto;
    String originPlan = "plan";
    BenefitCoverages benefitCoverages;
    List<String> coverageIds;
    List<BenefitReportPlansPojo> benefitReportPlansPojos;

    @BeforeEach
    void init() {
      typeName = "typeName";
      companyId = "companyId";
      benefitReportParamDto = new BenefitReportParamDto();
      benefitReportParamDto.setCoverageId("coverageId");
      benefitCoverages = new BenefitCoverages();
      benefitCoverages.setName("coverageName");
      benefitCoverages.setId("coverageId");
      coverageIds = new ArrayList<>();
      coverageIds.add("coverageId");
      benefitReportPlansPojos = new ArrayList<>();
    }

    @Test
    void whenPlanIdIsActive_thenShouldSuccess() {
      benefitReportParamDto.setPlanId("active");
      final List<String> benefitPlanIds = new ArrayList<>();
      benefitPlanIds.add("benefitPlanId");
      final List<EnrollmentBreakdownDto> enrollmentBreakdownDtos = new ArrayList<>();
      final EnrollmentBreakdownDto enrollmentBreakdownDto = new EnrollmentBreakdownDto();
      enrollmentBreakdownDto.setPlan(originPlan);
      enrollmentBreakdownDtos.add(enrollmentBreakdownDto);

      final BenefitReportPlansPojo benefitReportPlansPojo =
          new BenefitReportPlansPojo() {
            @Override
            public String getId() {
              return originPlan;
            }

            @Override
            public String getName() {
              return "name";
            }

            @Override
            public String getStatus() {
              return null;
            }
          };

      benefitReportPlansPojos.add(benefitReportPlansPojo);

      Mockito.when(benefitPlanRepository.getBenefitPlans(typeName, companyId))
          .thenReturn(benefitReportPlansPojos);

      Mockito.when(benefitPlanRepository.getActiveBenefitPlanIds(typeName, companyId))
          .thenReturn(benefitPlanIds);
      Mockito.when(benefitCoveragesRepository.findById("coverageId"))
          .thenReturn(Optional.of(benefitCoverages));
      Mockito.when(
              benefitCoveragesRepository.getCoverageIdsByNameAndPlan("typeName", benefitPlanIds))
          .thenReturn(coverageIds);

      Mockito.when(benefitPlanRepository.getEnrollmentBreakdown(benefitPlanIds, coverageIds))
          .thenReturn(enrollmentBreakdownDtos);

      final BenefitPlanReportDto benefitPlanReportDto =
          benefitPlanService.findBenefitPlanReport(typeName, benefitReportParamDto, companyId);
      final String plan = benefitPlanReportDto.getEnrollmentBreakdownDtos().get(0).getPlan();
      assertThat(plan).isEqualTo(originPlan);
    }

    @Test
    void whenPlanIdIsExpired_thenShouldSuccess() {
      benefitReportParamDto.setPlanId("expired");
      final List<String> benefitPlanIds = new ArrayList<>();
      benefitPlanIds.add("benefitPlanId");
      final List<EnrollmentBreakdownDto> enrollmentBreakdownDtos = new ArrayList<>();
      final EnrollmentBreakdownDto enrollmentBreakdownDto = new EnrollmentBreakdownDto();
      enrollmentBreakdownDto.setPlan(originPlan);
      enrollmentBreakdownDtos.add(enrollmentBreakdownDto);

      Mockito.when(benefitPlanRepository.getExpiredBenefitPlanIds(typeName, companyId))
          .thenReturn(benefitPlanIds);
      Mockito.when(benefitCoveragesRepository.findById("coverageId"))
          .thenReturn(Optional.of(benefitCoverages));
      Mockito.when(
              benefitCoveragesRepository.getCoverageIdsByNameAndPlan("typeName", benefitPlanIds))
          .thenReturn(coverageIds);

      Mockito.when(benefitPlanRepository.getEnrollmentBreakdown(benefitPlanIds, coverageIds))
          .thenReturn(enrollmentBreakdownDtos);

      final BenefitPlanReportDto benefitPlanReportDto =
          benefitPlanService.findBenefitPlanReport(typeName, benefitReportParamDto, companyId);
      final String plan = benefitPlanReportDto.getEnrollmentBreakdownDtos().get(0).getPlan();
      assertThat(plan).isEqualTo(originPlan);
    }

    @Test
    void whenPlanIdIsStarting_thenShouldSuccess() {
      benefitReportParamDto.setPlanId("starting");
      final List<String> benefitPlanIds = new ArrayList<>();
      benefitPlanIds.add("benefitPlanId");
      final List<EnrollmentBreakdownDto> enrollmentBreakdownDtos = new ArrayList<>();
      final EnrollmentBreakdownDto enrollmentBreakdownDto = new EnrollmentBreakdownDto();
      enrollmentBreakdownDto.setPlan(originPlan);
      enrollmentBreakdownDtos.add(enrollmentBreakdownDto);

      Mockito.when(benefitPlanRepository.getStartingBenefitPlanIds(typeName, companyId))
          .thenReturn(benefitPlanIds);
      Mockito.when(benefitCoveragesRepository.findById("coverageId"))
          .thenReturn(Optional.of(benefitCoverages));
      Mockito.when(
              benefitCoveragesRepository.getCoverageIdsByNameAndPlan("typeName", benefitPlanIds))
          .thenReturn(coverageIds);

      Mockito.when(benefitPlanRepository.getEnrollmentBreakdown(benefitPlanIds, coverageIds))
          .thenReturn(enrollmentBreakdownDtos);

      final BenefitPlanReportDto benefitPlanReportDto =
          benefitPlanService.findBenefitPlanReport(typeName, benefitReportParamDto, companyId);
      final String plan = benefitPlanReportDto.getEnrollmentBreakdownDtos().get(0).getPlan();
      assertThat(plan).isEqualTo(originPlan);
    }

    @Test
    void whenPlanIdIsNotEmpty_thenShouldSuccess() {
      benefitReportParamDto.setPlanId("benefitPlanId");
      final List<String> benefitPlanIds = new ArrayList<>();
      benefitPlanIds.add("benefitPlanId");
      final List<EnrollmentBreakdownDto> enrollmentBreakdownDtos = new ArrayList<>();
      final EnrollmentBreakdownDto enrollmentBreakdownDto = new EnrollmentBreakdownDto();
      enrollmentBreakdownDto.setPlan(originPlan);
      enrollmentBreakdownDtos.add(enrollmentBreakdownDto);
      Mockito.when(benefitPlanRepository.getActiveBenefitPlanIds(typeName, companyId))
          .thenReturn(benefitPlanIds);

      Mockito.when(benefitPlanCoverageRepository.getBenefitReportCoverages(benefitPlanIds))
          .thenReturn(new ArrayList<BenefitReportCoveragesDto>());
      Mockito.when(
              benefitPlanRepository.getEnrollmentBreakdown(
                  benefitPlanIds, Collections.singletonList("coverageId")))
          .thenReturn(enrollmentBreakdownDtos);
      final BenefitPlanReportDto benefitPlanReportDto =
          benefitPlanService.findBenefitPlanReport(typeName, benefitReportParamDto, companyId);
      final String plan = benefitPlanReportDto.getEnrollmentBreakdownDtos().get(0).getPlan();
      assertThat(plan).isEqualTo(originPlan);
    }
  }

  @Nested
  class getBenefitPlanList {
    String benefitPlanTypeId;
    String companyId;
    boolean expired;
    Pageable pageable;
    BenefitPlanSearchCondition benefitPlanSearchCondition;
    List<BenefitPlanPreviewPojo> benefitPlanPreviewPojos;
    Page<BenefitPlanPreviewPojo> benefitPlanPreviewDtoPage;

    @BeforeEach
    void init() {
      benefitPlanTypeId = "typeId";
      companyId = "companyId";
      expired = false;
      benefitPlanSearchCondition = new BenefitPlanSearchCondition();
      benefitPlanSearchCondition.setPage(0);
      benefitPlanSearchCondition.setSize(20);
      benefitPlanPreviewPojos = new ArrayList<>();
      benefitPlanSearchCondition.setSortDirection(
          EnrollmentBreakdownSearchCondition.SortDirection.ASC.name());
      benefitPlanSearchCondition.setSortField(
          EnrollmentBreakdownSearchCondition.SortField.NAME.name());
      pageable =
          PageRequest.of(
              benefitPlanSearchCondition.getPage(),
              benefitPlanSearchCondition.getSize(),
              Sort.Direction.valueOf(benefitPlanSearchCondition.getSortDirection().toUpperCase()),
              benefitPlanSearchCondition.getSortField().getSortValue());
      final BenefitPlanPreviewPojo benefitPlanPreviewPojo =
          new BenefitPlanPreviewPojo() {
            @Override
            public String getBenefitPlanId() {
              return "typeId";
            }

            @Override
            public String getBenefitPlanName() {
              return "name";
            }

            @Override
            public Timestamp getDeductionsBegin() {
              return null;
            }

            @Override
            public Timestamp getDeductionsEnd() {
              return null;
            }

            @Override
            public String getStatus() {
              return null;
            }

            @Override
            public Number getEligibleNumber() {
              return null;
            }

            @Override
            public Number getEnrolledNumber() {
              return null;
            }
          };
      benefitPlanPreviewPojos.add(benefitPlanPreviewPojo);
    }

    @Test
    void whenExpiredIsFalseGetList_thenShouldSuccess() {
      benefitPlanPreviewDtoPage = new PageImpl<>(benefitPlanPreviewPojos, pageable, 0);
      Mockito.when(
              benefitPlanRepository.getBenefitPlanListWithOutExpired(
                  benefitPlanTypeId, companyId, pageable))
          .thenReturn(benefitPlanPreviewDtoPage);
      benefitPlanService.findBenefitPlans(
          benefitPlanTypeId, companyId, expired, benefitPlanSearchCondition);
      Mockito.verify(benefitPlanRepository, Mockito.times(1))
          .getBenefitPlanListWithOutExpired(benefitPlanTypeId, companyId, pageable);
    }

    @Test
    void whenExpiredIsTrueGetList_thenShouldSuccess() {
      expired = true;
      benefitPlanPreviewDtoPage = new PageImpl<>(benefitPlanPreviewPojos, pageable, 0);
      Mockito.when(benefitPlanRepository.getBenefitPlanList(benefitPlanTypeId, companyId, pageable))
          .thenReturn(benefitPlanPreviewDtoPage);
      benefitPlanService.findBenefitPlans(
          benefitPlanTypeId, companyId, expired, benefitPlanSearchCondition);
      Mockito.verify(benefitPlanRepository, Mockito.times(1))
          .getBenefitPlanList(benefitPlanTypeId, companyId, pageable);
    }
  }

  @Nested
  class findBenefitPlanById {
    String id = "a";
    BenefitPlan benefitPlan;

    @BeforeEach
    void init() {
      benefitPlan = new BenefitPlan();
    }

    @Test
    void whenBenefitPlanFound_thenShouldThrow() {
      Mockito.when(benefitPlanRepository.findById(id))
          .thenReturn(java.util.Optional.ofNullable(benefitPlan));
      final BenefitPlan benefitPlan1 = benefitPlanService.findBenefitPlanById(id);
      assertThat(benefitPlan1).isNotNull();
    }

    @Test
    void whenBenefitPlanNotFound_thenShouldThrow() {
      Mockito.when(benefitPlanRepository.findById(id)).thenReturn(Optional.empty());
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> benefitPlanService.findBenefitPlanById(id));
    }
  }

  @Nested
  class clearBenefitPlansEnrollmentInfoByPlanType {
    BenefitPlanType.PlanType medicalType = BenefitPlanType.PlanType.MEDICAL;
    BenefitPlanType benefitPlanType;
    String companyId = "a";
    String userId = "a";
    List<BenefitPlan> benefitPlans;
    BenefitPlan benefitPlan;
    BenefitPlanUser benefitPlanUser;
    List<BenefitDependentRecord> benefitDependentRecords;

    @BeforeEach
    void init() {
      benefitPlanType = new BenefitPlanType();
      benefitPlanType.setName(medicalType.getValue());
      benefitPlanType.setId("a");
      benefitPlans = new ArrayList<>();
      benefitPlan = new BenefitPlan();
      benefitPlan.setId("a");
      benefitPlans.add(benefitPlan);
      benefitPlanUser = new BenefitPlanUser();
      benefitPlanUser.setId("a");
      benefitDependentRecords = new ArrayList<>();
    }

    @Test
    void clearBenefitPlansEnrollmentInfoByPlanType_thenShouldSuccess() throws Exception {
      Mockito.when(
              benefitPlanRepository.findByBenefitPlanTypeIdAndCompanyIdOrderByNameAsc(
                  benefitPlanType.getId(), companyId))
          .thenReturn(benefitPlans);
      Mockito.when(
              benefitPlanUserRepository.findByUserIdAndBenefitPlanId(userId, benefitPlan.getId()))
          .thenReturn(Optional.ofNullable(benefitPlanUser));
      Mockito.when(
              benefitPlanDependentRepository.findByBenefitPlansUsersId(benefitPlanUser.getId()))
          .thenReturn(benefitDependentRecords);
      Whitebox.invokeMethod(
          benefitPlanService,
          "clearBenefitPlansEnrollmentInfoByPlanType",
          benefitPlanType,
          companyId,
          userId);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(1))
          .findByUserIdAndBenefitPlanId(userId, benefitPlan.getId());
    }
  }

  @Nested
  class findBenefitCoveragesById {
    String id = "a";
    BenefitCoverages benefitCoverages;

    @BeforeEach
    void init() {
      benefitCoverages = new BenefitCoverages();
    }

    @Test
    void whenBenefitPlanFound_thenShouldThrow() {
      Mockito.when(benefitCoveragesRepository.findById(id))
          .thenReturn(java.util.Optional.ofNullable(benefitCoverages));
      final BenefitCoverages benefitCoverages1 = benefitPlanService.getBenefitCoveragesById(id);
      assertThat(benefitCoverages1).isNotNull();
    }

    @Test
    void whenBenefitPlanNotFound_thenShouldThrow() {
      Mockito.when(benefitCoveragesRepository.findById(id)).thenReturn(Optional.empty());
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> benefitPlanService.getBenefitCoveragesById(id));
    }
  }

  @Nested
  class getBenefitPlanCoverageById {
    String id = "a";
    BenefitPlanCoverage benefitPlanCoverage;

    @BeforeEach
    void init() {
      benefitPlanCoverage = new BenefitPlanCoverage();
    }

    @Test
    void whenBenefitPlanFound_thenShouldThrow() {
      Mockito.when(benefitPlanCoverageRepository.findById(id))
          .thenReturn(java.util.Optional.ofNullable(benefitPlanCoverage));
      final BenefitPlanCoverage benefitPlanCoverage1 =
          benefitPlanService.getBenefitPlanCoverageById(id);
      assertThat(benefitPlanCoverage1).isNotNull();
    }

    @Test
    void whenBenefitPlanNotFound_thenShouldThrow() {
      Mockito.when(benefitPlanCoverageRepository.findById(id)).thenReturn(Optional.empty());
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> benefitPlanService.getBenefitPlanCoverageById(id));
    }
  }

  @Nested
  class updateBenefitPlansEnrollmentInfoByPlanType {
    BenefitPlanType.PlanType medicalType = BenefitPlanType.PlanType.MEDICAL;
    BenefitPlanType benefitPlanType;
    SelectedEnrollmentInfoDto selectedEnrollmentInfoDto;
    String companyId = "a";
    String userId = "a";
    String benefitPlanId = "a";
    BenefitPlan benefitPlan;
    List<BenefitPlan> benefitPlans;
    BenefitPlanUser benefitPlanUser;

    @BeforeEach
    void init() {
      benefitPlanUser = new BenefitPlanUser();
      benefitPlanUser.setId("a");
      benefitPlan = new BenefitPlan();
      benefitPlan.setId("a");
      benefitPlanType = new BenefitPlanType();
      benefitPlanType.setId("a");
      selectedEnrollmentInfoDto = new SelectedEnrollmentInfoDto();
      selectedEnrollmentInfoDto.setBenefitPlanType("a");
      benefitPlans = new ArrayList<>();
      benefitPlans.add(benefitPlan);
    }

    @Test
    void whenBenefitPlanIdIsNotEqualEnrollmentPlanId_thenShouldSuccess() throws Exception {
      selectedEnrollmentInfoDto.setPlanId("a");
      Mockito.when(benefitPlanUserRepository.findByUserIdAndBenefitPlanId(userId, benefitPlanId))
          .thenReturn(Optional.ofNullable(benefitPlanUser));
      Mockito.when(benefitPlanTypeRepository.findByName("a")).thenReturn(benefitPlanType);
      Mockito.when(
              benefitPlanRepository.findByBenefitPlanTypeIdAndCompanyIdOrderByNameAsc(
                  "a", companyId))
          .thenReturn(benefitPlans);
      Whitebox.invokeMethod(
          benefitPlanService,
          "updateBenefitPlansEnrollmentInfoByPlanType",
          selectedEnrollmentInfoDto,
          companyId,
          userId);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(1))
          .findByUserIdAndBenefitPlanId(userId, benefitPlanId);
    }

    @Test
    void whenBenefitPlanIdIsEqualEnrollmentPlanId_thenShouldSuccess() throws Exception {
      selectedEnrollmentInfoDto.setPlanId("b");
      Mockito.when(benefitPlanUserRepository.findByUserIdAndBenefitPlanId(userId, benefitPlanId))
          .thenReturn(Optional.ofNullable(benefitPlanUser));
      benefitPlanUser.setEnrolled(false);
      benefitPlanUser.setBenefitPlanCoverage(null);
      Mockito.when(benefitPlanTypeRepository.findByName("a")).thenReturn(benefitPlanType);
      Mockito.when(
              benefitPlanRepository.findByBenefitPlanTypeIdAndCompanyIdOrderByNameAsc(
                  "a", companyId))
          .thenReturn(benefitPlans);
      Whitebox.invokeMethod(
          benefitPlanService,
          "updateBenefitPlansEnrollmentInfoByPlanType",
          selectedEnrollmentInfoDto,
          companyId,
          userId);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(1)).save(benefitPlanUser);
    }
  }

  @Nested
  class findAllEmployeesForBenefitPlan {
    String benefitPlanId;
    String companyId;
    List<BenefitPlanUserDto> allUsers;
    List<User> users;
    List<BenefitPlanUser> benefitPlanUsers;
    User user;
    BenefitPlanUser benefitPlanUser;
    BenefitPlanUserDto benefitPlanUserDto;
    JobUser jobUser;
    JobUserDto jobUserDto;
    Job job;

    @BeforeEach
    void init() {
      benefitPlanUserDto = new BenefitPlanUserDto();
      benefitPlanUserDto.setId("userId");
      benefitPlanId = "benefitPlanId";
      companyId = "companyId";
      allUsers = new ArrayList<>();
      users = new ArrayList<>();
      user = new User();
      user.setId("userId");
      users.add(user);
      final BenefitPlanUserDto benefitPlanUserDto = new BenefitPlanUserDto();
      benefitPlanUserDto.setId("userId");
      allUsers.add(benefitPlanUserDto);
      benefitPlanUsers = new ArrayList<>();
      benefitPlanUser = new BenefitPlanUser();
      benefitPlanUser.setId("benefitPlanUserId");
      final BenefitPlanCoverage benefitPlanCoverage = new BenefitPlanCoverage();
      benefitPlanCoverage.setId("benefitPlanCoverageId");
      benefitPlanUser.setBenefitPlanCoverage(benefitPlanCoverage);
      benefitPlanUsers.add(benefitPlanUser);
      jobUser = new JobUser();
      jobUser.setUser(user);
      jobUserDto = new JobUserDto();
      jobUserDto.setId("userId");
      jobUserDto.setJobTitle("jobTitle");
      job = new Job();
      job.setTitle("jobTitle");
      job.setCompany(new Company());
      jobUser.setJob(job);
    }

    @Test
    void whenFindAllEmployees_thenShouldSuccess() {
      Mockito.when(userMapper.covertToBenefitPlanUserDto(user)).thenReturn(benefitPlanUserDto);
      Mockito.when(benefitPlanUserMapper.convertToBenefitPlanUserDto(benefitPlanUser))
          .thenReturn(benefitPlanUserDto);
      Mockito.when(userRepository.findAllByCompanyId(companyId)).thenReturn(users);
      Mockito.when(benefitPlanUserRepository.findAllByBenefitPlanId(benefitPlanId))
          .thenReturn(benefitPlanUsers);
      Mockito.when(jobUserService.findJobUserByUser(user)).thenReturn(jobUser);
      Mockito.when(jobUserMapper.covertToBenefitPlanUserDto(jobUserDto))
          .thenReturn(benefitPlanUserDto);
      benefitPlanService.findAllEmployeesForBenefitPlan(benefitPlanId, companyId);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(1))
          .findAllByBenefitPlanId(benefitPlanId);
    }
  }

  @Nested
  class findAllCoveragesByBenefitPlan {
    String benefitPlanId;

    @BeforeEach
    void init() {
      benefitPlanId = "benefitPlanId";
    }

    @Test
    void whenFindAllCoverages_thenShouldSuccess() {
      benefitPlanService.findAllCoveragesByBenefitPlan(benefitPlanId);
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(1))
          .getBenefitPlanCoveragesByPlanId(benefitPlanId);
    }
  }

  @Nested
  class deleteBenefitPlan {
    String benefitPlanId;
    List<BenefitCoverages> benefitCoveragesList;
    BenefitCoverages benefitCoverages;
    List<String> ids;

    @BeforeEach
    void init() {
      benefitPlanId = "id";
      benefitCoveragesList = new ArrayList<>();
      benefitCoverages = new BenefitCoverages();
      benefitCoverages.setId("benefitPlanId");
      benefitCoveragesList.add(benefitCoverages);
      ids = new ArrayList<>();
      ids.add("benefitPlanId");
    }

    @Test
    void whenDeleteBenefitPlan_thenShouldSuccess() {
      Mockito.when(benefitCoveragesRepository.findAllByBenefitPlanId(benefitPlanId))
          .thenReturn(benefitCoveragesList);
      benefitPlanService.deleteBenefitPlanByPlanId(benefitPlanId);
      Mockito.verify(benefitCoveragesRepository, Mockito.times(1)).deleteInBatch(ids);
    }
  }

  @Nested
  class findAllPlans {
    String companyId = "companyId";
    List<BenefitPlan> benefitPlans;
    BenefitPlan benefitPlan;

    @BeforeEach
    void init() {
      benefitPlans = new ArrayList<>();
      benefitPlan = new BenefitPlan();
      benefitPlan.setId("planId");
      benefitPlan.setName("name");
      benefitPlans.add(benefitPlan);
    }

    @Test
    void whenFindAllPlans_thenShouldSuccess() {
      Mockito.when(benefitPlanRepository.findAllByCompanyId(companyId)).thenReturn(benefitPlans);
      Mockito.when(benefitPlanMapper.convertToBenefitPlanDto(benefitPlan))
          .thenReturn(new BenefitPlanDto());
      benefitPlanService.findAllPlans(companyId);
      Mockito.verify(benefitPlanMapper, Mockito.times(1)).convertToBenefitPlanDto(benefitPlan);
    }
  }

  @Nested
  class findPlansWhenPlanIdIsNull {
    final List<BenefitCoverages> coverages = new ArrayList<>();

    @Test
    void findPlansWhenPlanIdIsNull_thenShouldSuccess() {
      Mockito.when(benefitCoveragesRepository.findAllByBenefitPlanIdIsNull()).thenReturn(coverages);
      benefitPlanService.findPlansWhenPlanIdIsNull();
      Mockito.verify(benefitCoveragesRepository, Mockito.times(1)).findAllByBenefitPlanIdIsNull();
    }
  }
}
