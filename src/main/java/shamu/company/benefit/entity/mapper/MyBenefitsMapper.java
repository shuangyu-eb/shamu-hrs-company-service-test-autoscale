package shamu.company.benefit.entity.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.benefit.dto.BenefitPlanDependentUserDto;
import shamu.company.benefit.dto.BenefitSummaryDto;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface MyBenefitsMapper {

  @Mapping(target = "dependentUsers", source = "dependentUsers")
  BenefitSummaryDto convertToBenefitSummaryDto(
      Long benefitNumber,
      BigDecimal benefitCost,
      Long dependentNumber,
      List<BenefitPlanDependentUserDto> dependentUsers);
}
