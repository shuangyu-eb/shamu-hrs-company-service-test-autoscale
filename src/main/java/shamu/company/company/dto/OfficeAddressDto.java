package shamu.company.company.dto;

import lombok.Data;

@Data
public class OfficeAddressDto {

  private String street1;

  private String street2;

  private String city;

  private StateProvinceDto stateProvince;
}