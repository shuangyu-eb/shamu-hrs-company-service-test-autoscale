package shamu.company.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import shamu.company.user.entity.UserBenefitsSetting;
import shamu.company.user.repository.UserBenefitsSettingRepository;
import shamu.company.user.service.UserBenefitsSettingService;
import shamu.company.user.service.UserService;

import java.util.UUID;

class UserBenefitsSettingServiceTest {

  @Mock
  private UserBenefitsSettingRepository userBenefitsSettingRepository;

  @Mock
  private UserService userService;

  private UserBenefitsSettingService userBenefitsSettingService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    userBenefitsSettingService = new UserBenefitsSettingService(
      userBenefitsSettingRepository, userService);
  }

  @Test
  void findUserBenefitsHiddenBanner() {
    final UserBenefitsSetting userBenefitsSetting = new UserBenefitsSetting();
    userBenefitsSetting.setId("1");
    Mockito.when(userBenefitsSettingRepository.findByUserId(Mockito.any())).thenReturn(userBenefitsSetting);
    Assertions.assertDoesNotThrow(() ->
      userBenefitsSettingService.findUserBenefitsEffectYear("1", "1"));
  }

  @Test
  void save() {
    final UserBenefitsSetting userBenefitsSetting = new UserBenefitsSetting();
    userBenefitsSetting.setId("1");
    Mockito.when(userBenefitsSettingRepository.save(userBenefitsSetting)).thenReturn(userBenefitsSetting);
    Assertions.assertDoesNotThrow(() ->
      userBenefitsSettingService.save(userBenefitsSetting));
  }

  @Nested
  class BenefitsSettingEffectYear {
    private String userId;
    private String effectYear;

    @BeforeEach
    void init() {
      userId = UUID.randomUUID().toString().replaceAll("-", "");
      effectYear = "2020";
    }

    @Test
    void whenExist_shouldDoNothing() {
      Mockito.when(userBenefitsSettingRepository.findByUserAndEffectYear(Mockito.any(), Mockito.anyString()))
          .thenReturn(new UserBenefitsSetting());
      userBenefitsSettingService.saveUserBenefitsSettingEffectYear(userId, effectYear);
      Mockito.verify(userBenefitsSettingRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void whenNotExist_shouldSave() {
      Mockito.when(userBenefitsSettingRepository.findByUserAndEffectYear(Mockito.any(), Mockito.anyString()))
          .thenReturn(null);
      userBenefitsSettingService.saveUserBenefitsSettingEffectYear(userId, effectYear);
      Mockito.verify(userBenefitsSettingRepository, Mockito.times(1)).save(Mockito.any());

    }
  }
}
