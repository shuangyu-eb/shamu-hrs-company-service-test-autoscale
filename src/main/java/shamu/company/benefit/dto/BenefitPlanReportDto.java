package shamu.company.benefit.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenefitPlanReportDto {
  List<BenefitPlanReportSummaryDto> benefitPlanReportSummaryDtos;
  List<BenefitReportPlansDto> benefitReportPlansDtos;
  List<BenefitReportCoveragesDto> benefitReportCoveragesDtos;
  List<EnrollmentBreakdownDto> enrollmentBreakdownDtos;
}
