package shamu.company.benefit.entity.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.util.StringUtils;
import shamu.company.benefit.dto.BenefitPlanCreateDto;
import shamu.company.benefit.dto.BenefitPlanDetailDto;
import shamu.company.benefit.dto.BenefitPlanDto;
import shamu.company.benefit.dto.BenefitPlanUpdateDto;
import shamu.company.benefit.dto.BenefitPlanUserDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanCoverage;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.benefit.entity.RetirementPlanType;
import shamu.company.common.mapper.Config;

@Mapper(
    config = Config.class,
    uses = {
      BenefitPlanCoverageMapper.class,
      BenefitPlanUserMapper.class,
      RetirementPlanTypeMapper.class,
      BenefitPlanDocumentMapper.class
    })
public interface BenefitPlanMapper {

  BenefitPlanDto convertToBenefitPlanDto(BenefitPlan benefitPlan);

  @Mapping(target = "name", source = "planName")
  @Mapping(target = "benefitPlanType", source = "benefitPlanTypeId")
  BenefitPlan createFromBenefitPlanCreateDto(BenefitPlanCreateDto benefitPlanCreateDto);

  @Mapping(target = "name", source = "planName")
  @Mapping(target = "benefitPlanType", source = "benefitPlanTypeId")
  void updateFromBenefitPlanCreateDto(
      @MappingTarget BenefitPlan benefitPlan, BenefitPlanCreateDto benefitPlanCreateDto);

  @Mapping(target = "documents", source = "benefitPlanDocuments")
  BenefitPlanDetailDto concertTo(BenefitPlan benefitPlan);

  default BenefitPlanType convertFromBenefitPlanTypeId(final String benefitPlanTypeId) {

    return !StringUtils.isEmpty(benefitPlanTypeId) ? new BenefitPlanType(benefitPlanTypeId) : null;
  }

  @Mapping(target = "benefitPlanUsers", source = "benefitPlanUsers")
  @Mapping(target = "benefitPlan", source = "benefitPlan")
  BenefitPlanUpdateDto convertToOldBenefitPlanDto(
      BenefitPlan benefitPlan,
      List<BenefitPlanCoverage> benefitPlanCoverages,
      List<BenefitPlanUser> benefitPlanUsers,
      RetirementPlanType retirementPlanType);

  @Mapping(target = "benefitPlan", source = "benefitPlanDto")
  @Mapping(target = "benefitPlanUsers", source = "benefitPlanUsers")
  BenefitPlanUpdateDto convertToRBenefitRetirementDto(BenefitPlanDto benefitPlanDto, List<BenefitPlanUserDto> benefitPlanUsers);
}
