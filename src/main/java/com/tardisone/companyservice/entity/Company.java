package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "companies")
@NoArgsConstructor
public class Company extends BaseEntity {

    private String name;

    private String imageUrl;

    private String EIN;

    @OneToOne
    private CompanySize companySize;

    private String country;

    private String subdomainName;
}
