package shamu.company.benefit.entity.mapper;

import java.math.BigDecimal;
import org.mapstruct.Mapper;
import shamu.company.benefit.dto.BenefitPlanReportSummaryDto;
import shamu.company.benefit.dto.BenefitReportCoveragesDto;
import shamu.company.benefit.dto.BenefitReportParamDto;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface BenefitPlanReportMapper {

  BenefitPlanReportSummaryDto covertToBenefitPlanReportSummaryDto(
      String id, String name, BigDecimal number, String timeUnit);

  BenefitReportCoveragesDto covertToBenefitReportCoveragesDto(String id, String name);

  BenefitReportParamDto covertToBenefitReportParamDto(String planId, String coverageId);
}
