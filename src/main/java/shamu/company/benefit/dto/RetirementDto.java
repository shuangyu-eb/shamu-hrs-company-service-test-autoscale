package shamu.company.benefit.dto;

import lombok.Data;

@Data
public class RetirementDto {

  private Boolean isCompanyPercentage;

  private Boolean isDeductionLimit;

  private Boolean isEmployeePercentage;

  private String retirementTypeName;

}
