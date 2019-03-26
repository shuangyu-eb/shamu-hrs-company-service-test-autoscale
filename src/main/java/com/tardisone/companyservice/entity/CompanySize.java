package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "company_sizes")
@NoArgsConstructor
public class CompanySize extends BaseEntity{

    public CompanySize(String name) {
        this.name = name;
    }

    private String name;
}
