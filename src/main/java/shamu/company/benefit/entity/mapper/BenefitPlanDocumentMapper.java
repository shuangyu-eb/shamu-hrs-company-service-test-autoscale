package shamu.company.benefit.entity.mapper;

import org.mapstruct.Mapper;
import shamu.company.benefit.dto.BenefitPlanDocumentDto;
import shamu.company.benefit.entity.BenefitPlanDocument;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface BenefitPlanDocumentMapper {

  BenefitPlanDocumentDto convertToBenefitPlanDto(BenefitPlanDocument benefitPlanDocument);
}
