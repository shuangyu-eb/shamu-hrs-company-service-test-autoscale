package shamu.company.benefit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RetirementDto {

  private Boolean isEmployeePercentage;

  private Boolean isCompanyPercentage;

  private Boolean isDeductionLimit;

  private String retirementTypeName;

}
