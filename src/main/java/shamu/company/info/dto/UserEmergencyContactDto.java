package shamu.company.info.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.info.entity.UserEmergencyContact;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEmergencyContactDto extends BasicUserEmergencyContactDto {

  private String street1;

  private String street2;

  private String city;

  private String stateId;

  private String stateName;

  private String postalCode;

  private String countryId;

  private String countryName;

  public UserEmergencyContactDto(final UserEmergencyContact userEmergencyContact) {
    super(userEmergencyContact);
    street1 = userEmergencyContact.getStreet1();
    street2 = userEmergencyContact.getStreet2();
    city = userEmergencyContact.getCity();
    stateId =
        userEmergencyContact.getState() == null ? null : userEmergencyContact.getState().getId();
    stateName =
        userEmergencyContact.getState() == null ? null : userEmergencyContact.getState().getName();
    postalCode = userEmergencyContact.getPostalCode();
    countryId =
        userEmergencyContact.getState() == null
            || userEmergencyContact.getState().getCountry() == null
            ? null
            : userEmergencyContact.getState().getCountry().getId();
    countryName =
        userEmergencyContact.getState() == null
            || userEmergencyContact.getState().getCountry() == null
            ? null
            : userEmergencyContact.getState().getCountry().getName();
  }
}
