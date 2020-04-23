package shamu.company.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.service.UserCompensationService;

public class UserCompensationServiceTest {
  @Mock private UserCompensationRepository userCompensationRepository;

  @InjectMocks private UserCompensationService userCompensationService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenSave_thenShouldCall() {
    final UserCompensation userCompensation = new UserCompensation();
    userCompensationService.save(userCompensation);
    Mockito.verify(userCompensationRepository, Mockito.times(1)).save(Mockito.any());
  }
}
