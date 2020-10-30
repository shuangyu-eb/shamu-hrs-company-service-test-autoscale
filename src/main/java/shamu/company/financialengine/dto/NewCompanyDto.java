package shamu.company.financialengine.dto;

import lombok.Data;

@Data
public class NewCompanyDto {

  private String legalName;

  private String businessName;

  private String phoneNumber;

  private String industry;
}
