package shamu.company.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.config.annotations.CanEmpty;

@Data
@NoArgsConstructor
public class UserAddressDto {

  private String id;

  private String userId;

  private String street1;

  private String street2;

  @CanEmpty
  private String city;

  private String countryName;

  private String countryId;

  private String stateName;

  private String stateId;

  @CanEmpty
  private String postalCode;
}
