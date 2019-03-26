package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@Data
@NoArgsConstructor
public class UserContactInformation extends BaseEntity {

    private String phoneWork;

    private String phoneWorkExtension;

    private String phoneMobile;

    private String phoneHome;

    private String emailWork;

    private String emailHome;
}
