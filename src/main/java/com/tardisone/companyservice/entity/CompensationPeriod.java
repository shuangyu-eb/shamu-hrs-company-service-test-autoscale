package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "company_periods")
public class CompensationPeriod {

    @Id
    private Long id;

    @ManyToOne
    private Company company;

    private String name;
}
