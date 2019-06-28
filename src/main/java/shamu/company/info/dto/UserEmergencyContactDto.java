package shamu.company.info.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import shamu.company.common.entity.StateProvince;
import shamu.company.hashids.HashidsFormat;
import shamu.company.info.entity.UserEmergencyContact;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEmergencyContactDto extends BasicUserEmergencyContactDto {

  private String street1;

  private String street2;

  private String city;

  @HashidsFormat
  private Long stateId;

  private String postalCode;


  public UserEmergencyContactDto(UserEmergencyContact userEmergencyContact) {
    super(userEmergencyContact);
    this.street1 = userEmergencyContact.getStreet1();
    this.street2 = userEmergencyContact.getStreet2();
    this.city = userEmergencyContact.getCity();
    this.stateId =
        userEmergencyContact.getState() == null ? null : userEmergencyContact.getState().getId();
    this.postalCode = userEmergencyContact.getPostalCode();
  }

  @JSONField(serialize = false)
  public UserEmergencyContact getEmergencyContact() {
    UserEmergencyContact userEmergencyContact = new UserEmergencyContact();
    BeanUtils.copyProperties(this, userEmergencyContact);
    userEmergencyContact.setState(new StateProvince(this.stateId));
    return userEmergencyContact;
  }
}
