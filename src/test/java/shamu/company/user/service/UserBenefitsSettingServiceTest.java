package shamu.company.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import shamu.company.user.entity.UserBenefitsSetting;
import shamu.company.user.repository.UserBenefitsSettingRepository;

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
  void updateUserBenefitsHiddenBanner() {
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
}
