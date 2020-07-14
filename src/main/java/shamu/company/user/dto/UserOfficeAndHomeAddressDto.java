package shamu.company.user.dto;

import lombok.Builder;
import lombok.Data;
import shamu.company.company.dto.OfficeAddressDto;

@Builder
@Data
public class UserOfficeAndHomeAddressDto {

  private String userId;

  private UserAddressDto userHomeAddress;

  private OfficeAddressDto userOfficeAddress;

}
