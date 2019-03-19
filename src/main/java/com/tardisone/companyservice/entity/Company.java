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

    private String ein;

    @OneToOne
    private CompanySize companySize;

    private String country;

    private String subdomainName;
}
