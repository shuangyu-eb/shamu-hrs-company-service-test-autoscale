package com.tardisone.companyservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Where(clause = "deleted_at IS NULL")
public class User extends BaseEntity {

    private String employeeNumber;

    private String emailWork;

    private String password;

    private Timestamp latestLogin;

    @OneToOne
    private UserStatus userStatus;

    private String imageUrl;

    @ManyToOne
    private Company company;

    @ManyToOne
    @JsonIgnore
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
