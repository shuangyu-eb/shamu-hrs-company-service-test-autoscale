package shamu.company.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.user.repository.RetirementTypeRepository;
import shamu.company.user.service.RetirementTypeService;

public class RetirementTypeServiceTest {
  @Mock private RetirementTypeRepository retirementTypeRepository;

  @InjectMocks private RetirementTypeService retirementTypeService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenFindAll_thenShouldCall() {
    retirementTypeService.findAll();
    Mockito.verify(retirementTypeRepository, Mockito.times(1)).findAll();
  }
}
