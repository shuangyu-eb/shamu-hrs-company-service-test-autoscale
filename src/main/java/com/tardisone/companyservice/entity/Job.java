package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "jobs")
public class Job extends BaseEntity{

    private String title;

    @ManyToOne
    private Department department;
}
