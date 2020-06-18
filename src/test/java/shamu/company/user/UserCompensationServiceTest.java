package shamu.company.user;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
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

  @Nested
  class findTimeSheetById {
    UserCompensation userCompensation;

    @BeforeEach
    void init() {
      userCompensation = new UserCompensation();
    }

    @Test
    void whenIdExists_thenShouldSuccess() {
      Mockito.when(userCompensationRepository.findById(Mockito.anyString()))
          .thenReturn(Optional.ofNullable(userCompensation));
      assertThatCode(() -> userCompensationService.findCompensationById("1"))
          .doesNotThrowAnyException();
    }

    @Test
    void whenIdNotExists_thenShouldThrow() {
      Mockito.when(userCompensationRepository.findById(Mockito.anyString()))
          .thenReturn(Optional.empty());
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> userCompensationService.findCompensationById("1"));
    }
  }
}
