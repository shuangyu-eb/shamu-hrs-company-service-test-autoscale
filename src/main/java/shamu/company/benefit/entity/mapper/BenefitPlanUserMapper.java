package shamu.company.benefit.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.StringUtils;
import shamu.company.benefit.dto.BenefitPlanUserCreateDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.common.mapper.Config;
import shamu.company.user.entity.User;

@Mapper(config = Config.class)
public interface BenefitPlanUserMapper {

  @Mapping(target = "user", source = "benefitPlanUserCreateDto.id")
  @Mapping(target = "benefitPlan", source = "benefitPlanId")
  @Mapping(target = "enrolled", constant = "false")
  @Mapping(target = "id", ignore = true)
  BenefitPlanUser createFromBenefitPlanUserCreateDtoAndBenefitPlanId(
      BenefitPlanUserCreateDto benefitPlanUserCreateDto, String benefitPlanId);

  default User convertFromUserId(final String userId) {
    return !StringUtils.isEmpty(userId) ? new User(userId) : null;
  }

  default BenefitPlan convertFromBenefitPlanId(final String benefitPlanId) {
    return !StringUtils.isEmpty(benefitPlanId) ? new BenefitPlan(benefitPlanId) : null;
  }
}
