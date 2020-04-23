package shamu.company.timeoff.service;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.user.entity.User;

class TimeOffPolicyUserServiceTests {
  private static TimeOffPolicyUserService timeOffPolicyUserService;

  @Mock private TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    timeOffPolicyUserService = new TimeOffPolicyUserService(timeOffPolicyUserRepository);
  }

  @Test
  void findById() {
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    timeOffPolicyUser.setId("1");

    Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(timeOffPolicyUser));

    Assertions.assertEquals(
        timeOffPolicyUserService.findById("1").getId(), timeOffPolicyUser.getId());
    Assertions.assertDoesNotThrow(() -> timeOffPolicyUserService.findById("1"));
  }

  @Test
  void findById_whenEmpty_thenShouldThrow() {
    Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any())).thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> timeOffPolicyUserService.findById("1"));
  }

  @Test
  void existsByUserId() {
    boolean bol = timeOffPolicyUserService.existsByUserId("1");
    Assertions.assertFalse(bol);
  }

  @Test
  void findByUserAndTimeOffPolicy() {
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    timeOffPolicyUser.setId("1");

    Mockito.when(
            timeOffPolicyUserRepository.findTimeOffPolicyUserByUserAndTimeOffPolicy(
                Mockito.any(), Mockito.any()))
        .thenReturn(timeOffPolicyUser);
    Assertions.assertEquals(
        timeOffPolicyUserService.findByUserAndTimeOffPolicy(Mockito.any(), Mockito.any()).getId(),
        timeOffPolicyUser.getId());
    Assertions.assertDoesNotThrow(
        () ->
            timeOffPolicyUserService.findByUserAndTimeOffPolicy(
                new User("1"), new TimeOffPolicy("1")));
  }
}
