package shamu.company.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.user.repository.RetirementPayTypesRepository;
import shamu.company.user.service.RetirementPayTypeService;

public class RetirementPayTypeServiceTest {

  @Mock
  private RetirementPayTypesRepository retirementPayTypesRepository;

  @InjectMocks
  private RetirementPayTypeService retirementPayTypeService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenFindAll_thenShouldCall() {
    retirementPayTypeService.findAll();
    Mockito.verify(retirementPayTypesRepository, Mockito.times(1)).findAll();
  }

}
