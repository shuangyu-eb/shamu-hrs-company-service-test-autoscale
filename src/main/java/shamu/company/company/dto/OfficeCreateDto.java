package shamu.company.company.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import shamu.company.common.entity.StateProvince;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;

@Data
public class OfficeCreateDto {

  private String officeName;

  private String street1;

  private String street2;

  private String city;

  private String stateId;

  private String zip;

  @JSONField(serialize = false)
  public Office getOffice() {
    StateProvince stateProvince = null;
    if (!StringUtils.isEmpty(stateId)) {
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
