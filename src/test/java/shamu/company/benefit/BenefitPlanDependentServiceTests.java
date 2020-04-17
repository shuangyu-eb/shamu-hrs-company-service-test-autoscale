package shamu.company.benefit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.repository.UserDependentsRepository;
import shamu.company.benefit.service.BenefitPlanDependentService;

public class BenefitPlanDependentServiceTests {

  @Mock UserDependentsRepository userDependentsRepository;

  private BenefitPlanDependentService benefitPlanDependentService;

  private final BenefitPlanDependent benefitPlanDependent = new BenefitPlanDependent();

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    benefitPlanDependentService = new BenefitPlanDependentService(userDependentsRepository);
  }

  @Test
  void testCreateBenefitPlanDependent() {
    Assertions.assertDoesNotThrow(() -> benefitPlanDependentService.createBenefitPlanDependent(benefitPlanDependent));
  }

  @Test
  void testGetDependentListsByEmployeeId() {
    Assertions.assertDoesNotThrow(() -> benefitPlanDependentService.getDependentListsByEmployeeId("1"));
  }

  @Test
  void testUpdateDependentContact() {
    Assertions.assertDoesNotThrow(() -> benefitPlanDependentService.updateDependentContact(benefitPlanDependent));
  }

  @Test
  void testDeleteDependentContact() {
    Assertions.assertDoesNotThrow(() -> benefitPlanDependentService.deleteDependentContact("1"));
  }

  @Test
  void testFindDependentById() {
    Assertions.assertDoesNotThrow(() -> benefitPlanDependentService.findDependentById("1"));
  }
}
