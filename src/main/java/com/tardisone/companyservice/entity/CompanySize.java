package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "company_sizes")
public class CompanySize {

    @Id
    private Long id;

    private String name;
}
