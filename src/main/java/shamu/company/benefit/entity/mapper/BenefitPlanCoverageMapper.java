package shamu.company.benefit.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.benefit.dto.BenefitPlanCoverageDto;
import shamu.company.benefit.entity.BenefitCoverages;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanCoverage;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface BenefitPlanCoverageMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "benefitPlanId", source = "benefitPlan.id")
  @Mapping(target = "benefitCoverage", source = "newBenefitCoverage")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  BenefitPlanCoverage createFromBenefitPlanCoverageAndBenefitPlan(
      BenefitPlanCoverageDto benefitPlanCoverageDto, BenefitPlan benefitPlan,
      BenefitCoverages newBenefitCoverage);

  @Mapping(target = "id", source = "benefitPlanCoverageDto.id")
  @Mapping(target = "benefitPlanId", source = "benefitPlan.id")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  BenefitPlanCoverage updateFromBenefitPlanCoverageAndBenefitPlan(
          BenefitPlanCoverageDto benefitPlanCoverageDto, BenefitPlan benefitPlan);

  @Mapping(target = "coverageId", source = "benefitPlanCoverage.benefitCoverage.id")
  BenefitPlanCoverageDto convertToBenefitPlanCoverageDto(BenefitPlanCoverage benefitPlanCoverage);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "benefitCoverage", source = "benefitCoverages")
  @Mapping(target = "employeeCost", source = "benefitPlanCoverageDto.employeeCost")
  @Mapping(target = "employerCost", source = "benefitPlanCoverageDto.employerCost")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  BenefitPlanCoverage createFromBenefitPlanCoverageDtoAndCoverage(
          BenefitPlanCoverageDto benefitPlanCoverageDto, BenefitCoverages benefitCoverages);

  @Mapping(target = "id", source = "benefitPlanCoverageDto.id")
  @Mapping(target = "benefitCoverage", source = "benefitPlanCoverage.benefitCoverage")
  @Mapping(target = "employeeCost", source = "benefitPlanCoverageDto.employeeCost")
  @Mapping(target = "employerCost", source = "benefitPlanCoverageDto.employerCost")
  @Mapping(target = "createdAt", ignore = true)
  BenefitPlanCoverage createFromBenefitPlanCoverageDtoAndPlanCoverage(
          BenefitPlanCoverageDto benefitPlanCoverageDto, BenefitPlanCoverage benefitPlanCoverage);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "benefitCoverage", source = "benefitCoverage")
  @Mapping(target = "employeeCost", source = "benefitPlanCoverageDto.employeeCost")
  @Mapping(target = "employerCost", source = "benefitPlanCoverageDto.employerCost")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  BenefitPlanCoverage createFromBenefitPlanCoverageDto(
      BenefitPlanCoverageDto benefitPlanCoverageDto, BenefitCoverages benefitCoverage);
}
