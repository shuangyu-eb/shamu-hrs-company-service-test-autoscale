package shamu.company.company.dto;

import lombok.Data;
import shamu.company.company.entity.OfficeAddress;

@Data
public class OfficeAddressDto {

  private String street1;

  private String street2;

  private String city;

  private StateProvinceDto stateProvince;

  public OfficeAddressDto(final OfficeAddress officeAddressEntity) {
    setStreet1(officeAddressEntity.getStreet1());
    setStreet2(officeAddressEntity.getStreet2());
    setCity(officeAddressEntity.getCity());
    setStateProvince(new StateProvinceDto(officeAddressEntity.getStateProvince()));
  }
}
