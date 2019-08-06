package shamu.company.company.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.beans.BeanUtils;
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
    StateProvince stateProvince = null;
    if (stateId != null) {
      stateProvince = new StateProvince();
      stateProvince.setId(stateId);
    }
    final OfficeAddress officeAddress = new OfficeAddress();
    BeanUtils.copyProperties(this, officeAddress);
    officeAddress.setStateProvince(stateProvince);
    officeAddress.setPostalCode(zip);

    final Office office = new Office();
    office.setName(officeName);
    office.setOfficeAddress(officeAddress);

    return office;
  }
}
