package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
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

    @Column(name = "street_1")
    private String street1;

    @Column(name = "street_2")
    private String street2;

    @ManyToOne
    private City city;

    @ManyToOne
    private Country country;

    private String postalCode;
}
