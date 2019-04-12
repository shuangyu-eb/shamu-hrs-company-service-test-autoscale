package com.tardisone.companyservice.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class UserPersonalInformationDTO {
    private Long id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String preferredName;

    private Timestamp birthDate;

    private String ssn;

    private Long genderId;

    private String genderName;

    private Long maritalStatusId;

    private String maritalStatusName;

}
