package shamu.company.benefit.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BenefitPlanReportSummaryDto {

  String id;

  String name;

  BigDecimal number;

  String timeUnit;
}
