package com.tardisone.companyservice.dto;

import lombok.Data;

@Data
public class UserEmergencyContactDTO {

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

	public UserEmergencyContactDTO(String firstName, String lastName, String relationship, String phone, String email, String street1, String street2, String city, Long stateId, String postalCode, Boolean isPrimary) {

		this.firstName = firstName;
		this.lastName = lastName;
		this.relationship = relationship;
		this.phone = phone;
		this.email = email;
		this.street1 = street1;
		this.street2 = street2;
		this.city = city;
		this.stateId = stateId;
		this.postalCode = postalCode;
		this.isPrimary = isPrimary;

	}
}
