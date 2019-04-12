package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class ManagerUser extends BaseEntity {

	private String employeeNumber;

	private String emailWork;

	private String password;

	private Timestamp latestLogin;

	@OneToOne
	private UserStatus userStatus;

	private String imageUrl;

	@ManyToOne
	private Company company;

	@OneToOne
	private UserPersonalInformation userPersonalInformation;

	@OneToOne
	private UserContactInformation userContactInformation;

	@OneToOne
	private UserCompensation userCompensation;

	@OneToOne
	private UserRole userRole;

	private String invitationEmailToken;

	private Timestamp invitedAt;

	private Timestamp resetPasswordSentAt;

	private String resetPasswordToken;

	private String verificationToken;

	private Timestamp verifiedAt;
}
