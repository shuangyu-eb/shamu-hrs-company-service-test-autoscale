package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    @ManyToOne
    private Company company;

    private String name;
}
