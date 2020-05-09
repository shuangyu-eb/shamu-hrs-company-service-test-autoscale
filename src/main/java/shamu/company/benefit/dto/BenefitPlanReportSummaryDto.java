package shamu.company.benefit.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanReportSummaryDto {

  String id;

  String name;

  BigDecimal number;

  String timeUnit;
}
