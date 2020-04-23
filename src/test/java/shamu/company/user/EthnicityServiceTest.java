package shamu.company.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.user.repository.EthnicityRepository;
import shamu.company.user.service.EthnicityService;

public class EthnicityServiceTest {
  @Mock private EthnicityRepository ethnicityRepository;

  @InjectMocks private EthnicityService ethnicityService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenFindAll_thenShouldCall() {
    ethnicityService.findAll();
    Mockito.verify(ethnicityRepository, Mockito.times(1)).findAll();
  }
}
