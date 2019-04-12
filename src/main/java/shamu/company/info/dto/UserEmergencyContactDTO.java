package shamu.company.info.dto;

import shamu.company.info.entity.State;
import shamu.company.info.entity.UserEmergencyContact;
import lombok.Data;

@Data
public class UserEmergencyContactDTO {

	private Long id;

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

	public UserEmergencyContactDTO(UserEmergencyContact userEmergencyContact) {
		this.id = userEmergencyContact.getId();
		this.firstName = userEmergencyContact.getFirstName();
		this.lastName = userEmergencyContact.getLastName();
		this.relationship = userEmergencyContact.getRelationship();
		this.phone = userEmergencyContact.getPhone();
		this.email = userEmergencyContact.getEmail();
		this.street1 = userEmergencyContact.getStreet1();
		this.street2 = userEmergencyContact.getStreet2();
		this.city = userEmergencyContact.getCity();
		State state = userEmergencyContact.getState();
		Long stateId = (state != null) ? state.getId() : null;
		this.stateId = stateId;
		this.postalCode = userEmergencyContact.getPostalCode();
		this.isPrimary = userEmergencyContact.getIsPrimary();
	}
}
