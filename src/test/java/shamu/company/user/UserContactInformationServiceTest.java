package shamu.company.user;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.repository.UserContactInformationRepository;
import shamu.company.user.service.UserContactInformationService;

public class UserContactInformationServiceTest {
  @Mock private UserContactInformationRepository userContactInformationRepository;

  @InjectMocks private UserContactInformationService userContactInformationService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenNotFound_thenShouldThrow() {
    Mockito.when(userContactInformationRepository.findById(Mockito.anyString()))
        .thenReturn(Optional.empty());
    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> userContactInformationService.findUserContactInformationById("test"));
  }

  @Test
  void whenUpdate_thenShouldCall() {
    final UserContactInformation userContactInformation = new UserContactInformation();
    userContactInformationService.update(userContactInformation);
    Mockito.verify(userContactInformationRepository, Mockito.times(1)).save(Mockito.any());
  }

  @Test
  void whenSave_thenShouldCall() {
    final UserContactInformation userContactInformation = new UserContactInformation();
    userContactInformationService.save(userContactInformation);
    Mockito.verify(userContactInformationRepository, Mockito.times(1)).save(Mockito.any());
  }

  @Test
  void whenDelete_thenShouldCall() {
    final UserContactInformation userContactInformation = new UserContactInformation();
    userContactInformationService.delete(userContactInformation);
    Mockito.verify(userContactInformationRepository, Mockito.times(1))
        .delete(userContactInformation);
  }
}
