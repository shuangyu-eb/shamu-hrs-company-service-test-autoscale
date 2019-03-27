package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    private Long id;

    private String employeeNumber;

    private String emailWork;

    private String password;

    private Timestamp latestLogin;

//    @OneToOne
//    private UserStatus userStatus;

    private String imageUrl;

    @ManyToOne
    private Company company;

//    @OneToOne
//    private User managerUser;

    @OneToOne
    @JoinColumn(name = "user_personal_information_id", referencedColumnName = "id")
    private UserPersonalInformation userPersonalInformation;

    @OneToOne
    @JoinColumn(name = "user_contact_information_id", referencedColumnName = "id")
    private UserContactInformation userContactInformation;

//    @OneToOne
//    @JoinColumn(name = "user_compensation_id", referencedColumnName = "id")
//    private UserCompensation userCompensation;
//
//    @OneToOne
//    @JoinColumn(name = "user_role_id", referencedColumnName = "id")
//    private UserRole userRole;

    private String invitationEmailToken;

    private Timestamp invitedAt;

    private Timestamp resetPasswordSentAt;

    private String resetPasswordToken;

    private String verificationToken;

    private Timestamp verifiedAt;
}
