package shamu.company.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.user.entity.UserBenefitsSetting;
import shamu.company.user.repository.UserBenefitsSettingRepository;

@Service
@Transactional
public class UserBenefitsSettingService {

  private final UserBenefitsSettingRepository userBenefitsSettingRepository;

  public UserBenefitsSettingService(
      final UserBenefitsSettingRepository userBenefitsSettingRepository) {
    this.userBenefitsSettingRepository = userBenefitsSettingRepository;
  }

  public Boolean findUserBenefitsHiddenBanner(final String userId) {
    UserBenefitsSetting benefitsSetting = userBenefitsSettingRepository.findByUserId(userId);
    return benefitsSetting.getHiddenBanner();
  }

  public void updateUserBenefitsHiddenBanner(final String userId) {
    UserBenefitsSetting userBenefitsSetting = userBenefitsSettingRepository.findByUserId(userId);
    userBenefitsSetting.setHiddenBanner(true);
    userBenefitsSettingRepository.save(userBenefitsSetting);
  }

  public UserBenefitsSetting save(UserBenefitsSetting userBenefitsSetting) {
    return userBenefitsSettingRepository.save(userBenefitsSetting);
  }
}
