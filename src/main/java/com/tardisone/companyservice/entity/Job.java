package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "jobs")
public class Job extends BaseEntity {

    private String title;

    @ManyToOne
    private Department department;
}
