package shamu.company.benefit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.benefit.dto.BenefitPlanDependentUserDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanCoverage;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.benefit.entity.mapper.MyBenefitsMapper;
import shamu.company.benefit.repository.BenefitPlanCoverageRepository;
import shamu.company.benefit.repository.BenefitPlanRepository;
import shamu.company.benefit.repository.BenefitPlanUserRepository;
import shamu.company.benefit.service.BenefitPlanService;

class BenefitPlanServiceTests {

  @InjectMocks private BenefitPlanService benefitPlanService;

  @Mock private BenefitPlanUserRepository benefitPlanUserRepository;

  @Mock private MyBenefitsMapper myBenefitsMapper;

  @Mock private BenefitPlanRepository benefitPlanRepository;

  @Mock private BenefitPlanCoverageRepository benefitPlanCoverageRepository;

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
  class getBenefitPlanReport {
    String typeName = "Medical";
    String companyId = "a";
    List<String> planIds;

    @BeforeEach
    void init() {
      planIds = new ArrayList<>();
    }

    @Test
    void whenBenefitPlanIdsIsEmpty_thenShouldSuccess() {
      Mockito.when(benefitPlanRepository.getBenefitPlanIds(typeName, companyId))
          .thenReturn(planIds);
      benefitPlanService.getBenefitPlanReport(typeName, companyId);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(0))
          .getEmployeesEnrolledNumber(Mockito.anyList());
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(0))
          .getCompanyCost(Mockito.anyList());
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(0))
          .getEmployeeCost(Mockito.anyList());
    }

    @Test
    void whenBenefitPlanIdsIsNotEmpty_thenShouldSuccess() {
      planIds.add("a");
      Mockito.when(benefitPlanRepository.getBenefitPlanIds(typeName, companyId))
          .thenReturn(planIds);
      benefitPlanService.getBenefitPlanReport(typeName, companyId);
      Mockito.verify(benefitPlanUserRepository, Mockito.times(1))
          .getEmployeesEnrolledNumber(Mockito.anyList());
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(1))
          .getCompanyCost(Mockito.anyList());
      Mockito.verify(benefitPlanCoverageRepository, Mockito.times(1))
          .getEmployeeCost(Mockito.anyList());
    }
  }
}
