package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
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

    private String emailWork;

    private Timestamp birthDate;

    private String ssn;

    private Long genderId;

    private Long maritalStatusId;

    private Long ethnicityId;

    private Long citizenshipStatusId;
}
