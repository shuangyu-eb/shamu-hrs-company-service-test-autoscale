package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class UserContactInformation {

    @Id
    private Long id;

    private String phoneWork;

    private String phoneWorkExtension;

    private String phoneMobile;

    private String phoneHome;

    private String emailWork;

    private String emailHome;
}
