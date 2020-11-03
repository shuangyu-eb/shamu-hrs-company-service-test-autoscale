package shamu.company.financialengine.dto;

import lombok.Data;

// Receive front end data
@Data
public class NewFECompanyInformationDto {

  private String mailingAddress;

  private String filingAddress;

  private String legalName;

  private String businessName;

  private String phoneNumber;

  private String industry;
}
