package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
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

    @ManyToOne
    private Gender gender;

    @ManyToOne
    private MaritalStatus maritalStatus;

    @ManyToOne
    private Ethnicity ethnicity;

    @ManyToOne
    private CitizenshipStatus citizenshipStatus;
}
