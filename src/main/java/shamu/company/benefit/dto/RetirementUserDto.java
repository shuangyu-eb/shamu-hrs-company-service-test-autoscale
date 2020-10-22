package shamu.company.benefit.dto;

import lombok.Data;

@Data
public class RetirementUserDto {

  private Double annualMaximum;

  private String companyContribution;

  private Double contributionValue;

  private Double deductionValue;

  private String employeeDeduction;

  private Boolean isDeductionLimit;

}
