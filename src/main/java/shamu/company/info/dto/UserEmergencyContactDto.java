package shamu.company.info.dto;

import lombok.Data;
import shamu.company.info.entity.UserEmergencyContact;

@Data
public class UserEmergencyContactDto {

  private Long id;

  private Long userId;

  private String firstName;

  private String lastName;

  private String relationship;

  private String phone;

  private String email;

  private String street1;

  private String street2;

  private String city;

  private Long stateId;

  private String postalCode;

  private Boolean isPrimary;

  public UserEmergencyContactDto(UserEmergencyContact userEmergencyContact) {
    this.id = userEmergencyContact.getId();
    this.userId = userEmergencyContact.getUserId();
    this.firstName = userEmergencyContact.getFirstName();
    this.lastName = userEmergencyContact.getLastName();
    this.relationship = userEmergencyContact.getRelationship();
    this.phone = userEmergencyContact.getPhone();
    this.email = userEmergencyContact.getEmail();
    this.street1 = userEmergencyContact.getStreet1();
    this.street2 = userEmergencyContact.getStreet2();
    this.city = userEmergencyContact.getCity();
    this.stateId = userEmergencyContact.getStateId();
    this.postalCode = userEmergencyContact.getPostalCode();
    this.isPrimary = userEmergencyContact.getIsPrimary();
  }
}
