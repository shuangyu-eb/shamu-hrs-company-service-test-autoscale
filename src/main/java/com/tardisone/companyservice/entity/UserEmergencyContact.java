package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user_emergency_contacts")
@Data
@NoArgsConstructor
public class UserEmergencyContact extends BaseEntity {

	private Long user_id;

	private Boolean isPrimary = false;

	private String firstName;

	private String lastName;

	private String relationship;

	private String phone;

	private String email;

}
