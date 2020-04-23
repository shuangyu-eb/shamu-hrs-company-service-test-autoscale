package shamu.company.benefit;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.repository.BenefitPlanTypeRepository;
import shamu.company.benefit.service.BenefitPlanTypeService;
import shamu.company.common.exception.ResourceNotFoundException;

public class BenefitPlanTypeServiceTests {

  @Mock private BenefitPlanTypeRepository benefitPlanTypeRepository;

  private BenefitPlanTypeService benefitPlanTypeService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    benefitPlanTypeService = new BenefitPlanTypeService(benefitPlanTypeRepository);
  }

  @Test
  void testFindAllBenefitPlanTypes() {
    Assertions.assertDoesNotThrow(() -> benefitPlanTypeService.findAllBenefitPlanTypes());
  }

  @Nested
  class testFindBenefitPlanTypeById {
    @Test
    void whenIdExists_thenShouldSuccess() {
      final Optional<BenefitPlanType> optional = Optional.of(new BenefitPlanType());
      Mockito.when(benefitPlanTypeRepository.findById(Mockito.anyString())).thenReturn(optional);
      Assertions.assertDoesNotThrow(() -> benefitPlanTypeService.findBenefitPlanTypeById("1"));
    }

    @Test
    void whenIdNotExists_thenShouldThrow() {
      final Optional<BenefitPlanType> optional = Optional.empty();
      Mockito.when(benefitPlanTypeRepository.findById(Mockito.anyString())).thenReturn(optional);
      Assertions.assertThrows(
          ResourceNotFoundException.class,
          () -> benefitPlanTypeService.findBenefitPlanTypeById("1"));
    }
  }
}
