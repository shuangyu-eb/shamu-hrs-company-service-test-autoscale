package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "user_emergency_contacts")
@Data
@NoArgsConstructor
public class UserEmergencyContact extends BaseEntity {

	@ManyToOne
	private User user;

	private String firstName;

	private String lastName;

	private String relationship;

	private String phone;

	private String email;

	@Column(name = "street_1")
	private String street1;

	@Column(name = "street_2")
	private String street2;

	private String city;

	@ManyToOne
	private State state;

	private String postalCode;

	private Boolean isPrimary = false;
}
