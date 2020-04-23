package shamu.company.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserBenefitsSetting;
import shamu.company.user.repository.UserBenefitsSettingRepository;

@Service
@Transactional
public class UserBenefitsSettingService {

  private final UserBenefitsSettingRepository userBenefitsSettingRepository;

  private final UserService userService;

  public UserBenefitsSettingService(
      final UserBenefitsSettingRepository userBenefitsSettingRepository,
      final UserService userService) {
    this.userBenefitsSettingRepository = userBenefitsSettingRepository;
    this.userService = userService;
  }

  public Boolean findUserBenefitsEffectYear(final String userId, final String effectYear) {
    final User user = userService.findById(userId);
    final UserBenefitsSetting benefitsSetting =
        userBenefitsSettingRepository.findByUserAndEffectYear(user, effectYear);
    return benefitsSetting != null;
  }

  public void saveUserBenefitsSettingEffectYear(final String userId, final String effectYear) {
    final User user = userService.findById(userId);
    final UserBenefitsSetting currentYearUserBenefitsSetting =
        userBenefitsSettingRepository.findByUserAndEffectYear(user, effectYear);
    if (currentYearUserBenefitsSetting == null) {
      final UserBenefitsSetting userBenefitsSetting = new UserBenefitsSetting();
      userBenefitsSetting.setUser(user);
      userBenefitsSetting.setEffectYear(effectYear);
      save(userBenefitsSetting);
      final Integer lastYear = Integer.parseInt(effectYear) - 1;
      final UserBenefitsSetting lastYearUserBenefitsSetting =
          userBenefitsSettingRepository.findByUserAndEffectYear(user, String.valueOf(lastYear));
      if (lastYearUserBenefitsSetting != null) {
        userBenefitsSettingRepository.delete(lastYearUserBenefitsSetting);
      }
    }
  }

  public UserBenefitsSetting save(final UserBenefitsSetting userBenefitsSetting) {
    return userBenefitsSettingRepository.save(userBenefitsSetting);
  }
}
