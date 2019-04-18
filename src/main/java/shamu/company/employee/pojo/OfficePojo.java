package shamu.company.employee.pojo;

import lombok.Data;
import shamu.company.common.entity.StateProvince;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;

@Data
public class OfficePojo {

  private String officeName;

  private String street1;

  private String street2;

  private String city;

  private String state;

  private String zip;

  public OfficeAddress getOfficeAddress() {
    OfficeAddress officeAddress = new OfficeAddress();
    officeAddress.setCity(getCity());
    if (!"".equals(getState()) && null != getState()) {
      StateProvince state = new StateProvince();
      state.setId(Long.parseLong(getState()));
      officeAddress.setStateProvince(state);
    }
    officeAddress.setPostalCode(getZip());
    officeAddress.setStreet1(getStreet1());
    officeAddress.setStreet2(getStreet2());
    return officeAddress;
  }

  public Office getOffice() {
    Office office = new Office();
    office.setName(getOfficeName());
    return office;
  }
}
