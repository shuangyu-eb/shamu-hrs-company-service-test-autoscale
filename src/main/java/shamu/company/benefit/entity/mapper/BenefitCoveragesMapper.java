package shamu.company.benefit.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.benefit.dto.BenefitCoveragesDto;
import shamu.company.benefit.dto.BenefitPlanCoverageDto;
import shamu.company.benefit.entity.BenefitCoverages;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface BenefitCoveragesMapper {

  @Mapping(target = "coverageName", source = "name")
  BenefitCoveragesDto convertToBenefitCoveragesDto(BenefitCoverages benefitCoverages);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "coverageName")
  BenefitCoverages createFromBenefitPlanCoverageDto(BenefitPlanCoverageDto benefitPlanCoverageDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", source = "benefitPlanCoverageDto.coverageName")
  @Mapping(target = "benefitPlanId", source = "benefitPlan.id")
  BenefitCoverages createFromBenefitPlanCoverageDtoAndPlan(
      BenefitPlanCoverageDto benefitPlanCoverageDto, BenefitPlan benefitPlan);
}
