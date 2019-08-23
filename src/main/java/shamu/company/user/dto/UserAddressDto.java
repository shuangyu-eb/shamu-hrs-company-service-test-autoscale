package shamu.company.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;

@Data
@NoArgsConstructor
public class UserAddressDto {

  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long userId;

  private String street1;

  private String street2;

  private String city;

  private String countryName;

  @HashidsFormat
  private Long countryId;

  private String stateProvinceName;

  @HashidsFormat
  private Long stateProvinceId;

  private String postalCode;
}
