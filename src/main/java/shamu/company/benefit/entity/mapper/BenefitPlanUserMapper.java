package shamu.company.benefit.entity.mapper;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.StringUtils;
import shamu.company.benefit.dto.BenefitPlanUserCreateDto;
import shamu.company.benefit.dto.BenefitPlanUserDto;
import shamu.company.benefit.dto.UserBenefitPlanDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.common.mapper.Config;
import shamu.company.user.entity.User;

@Mapper(config = Config.class)
public interface BenefitPlanUserMapper {

  @Mapping(target = "user", source = "benefitPlanUserCreateDto.id")
  @Mapping(target = "benefitPlan", source = "benefitPlanId")
  @Mapping(target = "id", ignore = true)
  BenefitPlanUser createFromBenefitPlanUserCreateDtoAndBenefitPlanId(
      BenefitPlanUserCreateDto benefitPlanUserCreateDto, String benefitPlanId);

  @Mapping(target = "title", source = "benefitPlan.name")
  @Mapping(target = "benefitPlanId", source = "benefitPlan.id")
  @Mapping(target = "benefitPlanStartDate", source = "benefitPlan.startDate")
  @Mapping(target = "benefitPlanEndDate", source = "benefitPlan.endDate")
  @Mapping(target = "type", source = "benefitPlan.benefitPlanType.name")
  @Mapping(target = "coverageId", source = "benefitPlanCoverage.id")
  @Mapping(target = "cost", source = "benefitPlanCoverage.employeeCost")
  @Mapping(target = "employerCost", source = "benefitPlanCoverage.employerCost")
  @Mapping(target = "enrolled", source = "enrolled")
  @Mapping(target = "coverageOptions", source = "benefitPlan.coverages")
  @Mapping(target = "dependents", source = "benefitPlanUser.benefitPlanDependents")
  UserBenefitPlanDto convertFrom(BenefitPlanUser benefitPlanUser);

  default User convertFromUserId(final String userId) {
    return !StringUtils.isEmpty(userId) ? new User(userId) : null;
  }

  default BenefitPlan convertFromBenefitPlanId(final String benefitPlanId) {
    return !StringUtils.isEmpty(benefitPlanId) ? new BenefitPlan(benefitPlanId) : null;
  }

  @Mapping(target = "firstName", source = "user.userPersonalInformation.firstName")
  @Mapping(target = "lastName", source = "user.userPersonalInformation.lastName")
  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "imageUrl", source = "user.imageUrl")
  BenefitPlanUserDto convertToBenefitPlanUserDto(BenefitPlanUser benefitPlanUser);
}
