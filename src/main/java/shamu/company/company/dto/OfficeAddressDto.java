package shamu.company.company.dto;

import lombok.Data;

@Data
public class OfficeAddressDto {

  private String street1;

  private String street2;

  private String city;

  private String postalCode;

  private String stateId;

  private String stateName;

  private String countryId;

  private String countryName;
}
