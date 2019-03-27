package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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

    @OneToOne
    @JoinColumn(name = "gender_id", referencedColumnName = "id")
    private Gender gender;

    @OneToOne
    @JoinColumn(name = "marital_status_id", referencedColumnName = "id")
    private MartialStatus martialStatus;

    private Long ethnicityId;

    private Long citizenshipStatusId;
}
