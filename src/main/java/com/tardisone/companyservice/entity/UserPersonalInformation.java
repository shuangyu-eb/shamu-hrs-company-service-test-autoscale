package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
public class UserPersonalInformation {

    @Id
    private Long id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String preferredName;

    private Timestamp birthDate;

    private String ssn;

    @ManyToOne
    private Gender gender;

    @OneToOne
    private MartialStatus martialStatus;

    private Long ethnicityId;

    private Long citizenshipStatusId;
}
