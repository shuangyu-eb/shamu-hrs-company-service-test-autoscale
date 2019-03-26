package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Entity
@Table(name = "companies")
@NoArgsConstructor
public class Company extends BaseEntity {

    private String name;

    private String imageUrl;

    private String EIN;

    @OneToOne
    private CompanySize companySize;

    @ManyToOne
    private Country country;

    private String subdomainName;
}
