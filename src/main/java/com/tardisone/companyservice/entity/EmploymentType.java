package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "employment_types")
public class EmploymentType {
    @Id
    private Long id;

    private String name;
}
