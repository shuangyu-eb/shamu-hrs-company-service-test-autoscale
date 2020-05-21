package shamu.company.benefit;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.repository.UserDependentsRepository;
import shamu.company.benefit.service.BenefitPlanDependentService;

public class BenefitPlanDependentServiceTests {

  private final BenefitPlanDependent benefitPlanDependent = new BenefitPlanDependent();
  @Mock UserDependentsRepository userDependentsRepository;
  private BenefitPlanDependentService benefitPlanDependentService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    benefitPlanDependentService = new BenefitPlanDependentService(userDependentsRepository);
  }

  @Test
  void testCreateBenefitPlanDependent() {
    assertThatCode(
            () -> benefitPlanDependentService.createBenefitPlanDependent(benefitPlanDependent))
        .doesNotThrowAnyException();
  }

  @Test
  void testGetDependentListsByEmployeeId() {
    assertThatCode(() -> benefitPlanDependentService.getDependentListsByEmployeeId("1"))
        .doesNotThrowAnyException();
  }

  @Test
  void testUpdateDependentContact() {
    assertThatCode(() -> benefitPlanDependentService.updateDependentContact(benefitPlanDependent))
        .doesNotThrowAnyException();
  }

  @Test
  void testDeleteDependentContact() {
    assertThatCode(() -> benefitPlanDependentService.deleteDependentContact("1"))
        .doesNotThrowAnyException();
  }

  @Test
  void testFindDependentById() {
    assertThatCode(() -> benefitPlanDependentService.findDependentById("1"))
        .doesNotThrowAnyException();
  }
}
