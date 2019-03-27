package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
public class UserPersonalInformation extends BaseEntity{

    private String firstName;

    private String middleName;

    private String lastName;

    private String preferredName;

    private Timestamp birthDate;

    private String ssn;

    private Long genderId;

    private Long maritalStatusId;

    private Long ethnicityId;

    private Long citizenshipStatusId;
}
