package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
public class UserPersonalInformation extends BaseEntity{

//    private String firstName;
//
//    private String middleName;
//
//    private String lastName;
//
//    private String preferredName;
//
//    private Timestamp birthDate;
//
//    private String ssn;
//
//    private Long genderId;
//
//    private Long maritalStatusId;
//
//    private Long ethnicityId;
//
//    private Long citizenshipStatusId;


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
