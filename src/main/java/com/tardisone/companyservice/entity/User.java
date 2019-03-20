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

    @OneToOne
    private UserStatus userStatus;

    private String imageUrl;

    @ManyToOne
    private Company company;

    @OneToOne
    private User managerUser;

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
