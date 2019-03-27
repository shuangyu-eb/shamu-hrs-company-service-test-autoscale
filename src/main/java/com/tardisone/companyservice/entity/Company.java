package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "companies")
public class Company {

    @Id
    private Long id;

    private String name;

    private String imageUrl;

    private String EIN;

    @OneToOne
    private CompanySize companySize;

    @OneToOne
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    private Country country;

    private String subdomainName;
}
