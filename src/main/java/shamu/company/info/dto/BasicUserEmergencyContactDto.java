package shamu.company.info.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;
import shamu.company.info.entity.UserEmergencyContact;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicUserEmergencyContactDto {

  @HashidsFormat
  public Long id;

  @HashidsFormat
  public Long userId;

  public String firstName;

  public String lastName;

  public String relationship;

  public String phone;

  public String email;

  public Boolean isPrimary;


  public BasicUserEmergencyContactDto(UserEmergencyContact userEmergencyContact) {
    this.id = userEmergencyContact.getId();
    this.userId = userEmergencyContact.getUser().getId();
    this.firstName = userEmergencyContact.getFirstName();
    this.lastName = userEmergencyContact.getLastName();
    this.relationship = userEmergencyContact.getRelationship();
    this.phone = userEmergencyContact.getPhone();
    this.email = userEmergencyContact.getEmail();
    this.isPrimary = userEmergencyContact.getIsPrimary();
  }
}
