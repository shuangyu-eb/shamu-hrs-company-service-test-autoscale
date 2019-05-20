package shamu.company.company.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import shamu.company.common.entity.StateProvince;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.hashids.HashidsFormat;

@Data
public class OfficeCreateDto {

  private String officeName;

  private String street1;

  private String street2;

  private String city;

  @HashidsFormat
  private Long stateId;

  private String zip;

  @JSONField(serialize = false)
  public Office getOffice() {
    OfficeAddress officeAddress = new OfficeAddress();
    StateProvince stateProvince = null;
    if (stateId != null) {
      stateProvince = new StateProvince();
      stateProvince.setId(stateId);
    }
    officeAddress.setCity(city);
    officeAddress.setStateProvince(stateProvince);
    officeAddress.setStreet1(street1);
    officeAddress.setStreet2(street2);
    officeAddress.setPostalCode(zip);

    Office office = new Office();
    office.setName(officeName);
    office.setOfficeAddress(officeAddress);

    return office;
  }
}
