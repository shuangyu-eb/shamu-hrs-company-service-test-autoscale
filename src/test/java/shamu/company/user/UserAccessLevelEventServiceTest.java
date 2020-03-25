package shamu.company.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.user.entity.UserAccessLevelEvent;
import shamu.company.user.repository.UserAccessLevelEventRepository;
import shamu.company.user.service.UserAccessLevelEventService;

public class UserAccessLevelEventServiceTest {
  @Mock
  private UserAccessLevelEventRepository userAccessLevelEventRepository;

  @InjectMocks
  private UserAccessLevelEventService userAccessLevelEventService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenSave_thenShouldCall() {
    final UserAccessLevelEvent userAccessLevelEvent = new UserAccessLevelEvent();
    userAccessLevelEventService.save(userAccessLevelEvent);
    Mockito.verify(userAccessLevelEventRepository, Mockito.times(1)).save(Mockito.any());
  }
}
