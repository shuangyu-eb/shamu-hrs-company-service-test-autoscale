package shamu.company.user.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.UserBenefitsSetting;

public interface UserBenefitsSettingRepository extends BaseRepository<UserBenefitsSetting, String> {

  UserBenefitsSetting findByUserId(String id);
}
