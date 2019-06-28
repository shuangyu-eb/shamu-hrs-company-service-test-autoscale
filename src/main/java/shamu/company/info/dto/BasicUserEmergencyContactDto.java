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
  private Long id;

  @HashidsFormat
  private Long userId;

  private String firstName;

  private String lastName;

  private String relationship;

  private String phone;

  private String email;

  private Boolean isPrimary;


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
