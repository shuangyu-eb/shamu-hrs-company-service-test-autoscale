package shamu.company.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserStatusService;

public class UserStatusServiceTest {
  @Mock private UserStatusRepository userStatusRepository;

  @InjectMocks private UserStatusService userStatusService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenFindByName_thenShouldCall() {
    userStatusService.findByName("test");
    Mockito.verify(userStatusRepository, Mockito.times(1)).findByName(Mockito.anyString());
  }

  @Test
  void whenFindAll_thenShouldCall() {
    userStatusService.findAll();
    Mockito.verify(userStatusRepository, Mockito.times(1)).findAll();
  }
}
