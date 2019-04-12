package shamu.company.info.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user_emergency_contacts")
@Data
@NoArgsConstructor
public class UserEmergencyContact extends BaseEntity {

	private Long userId;

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

	private Long stateId;

	private String postalCode;

	private Boolean isPrimary = false;
}
