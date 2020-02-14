package shamu.company.benefit.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BenefitPlanReportDto {
  List<BenefitPlanReportSummaryDto> benefitPlanReportSummaryDtos;
  List<BenefitReportPlansDto> benefitReportPlansDtos;
  List<BenefitReportCoveragesDto> benefitReportCoveragesDtos;
  List<EnrollmentBreakdownDto> enrollmentBreakdownDtos;
}
