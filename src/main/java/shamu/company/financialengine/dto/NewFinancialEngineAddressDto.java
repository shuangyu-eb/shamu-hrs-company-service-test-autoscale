package shamu.company.financialengine.dto;

import lombok.Data;

@Data
public class NewFinancialEngineAddressDto {

  private String companyId;

  private String addressType;

  private String street1;

  private String street2;

  private String city;

  private String statesProvince;

  private String postalCode;

  private String country;
}
