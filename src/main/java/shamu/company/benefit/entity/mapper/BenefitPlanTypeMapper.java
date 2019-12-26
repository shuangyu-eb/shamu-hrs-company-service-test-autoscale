package shamu.company.benefit.entity.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.common.mapper.Config;
import shamu.company.employee.dto.SelectFieldInformationDto;

@Mapper(config = Config.class)
public interface BenefitPlanTypeMapper {

  List<SelectFieldInformationDto> convertAllToDefaultBenefitPlanTypeDtos(
      List<BenefitPlanType> benefitPlanTypes);
}
