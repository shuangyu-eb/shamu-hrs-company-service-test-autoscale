package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "departments")
public class Department extends BaseEntity{

    @ManyToOne
    private Company company;

    private String name;

}
