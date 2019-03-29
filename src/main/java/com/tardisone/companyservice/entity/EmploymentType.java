package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "employment_types")
public class EmploymentType extends BaseEntity {

    private String name;
}
