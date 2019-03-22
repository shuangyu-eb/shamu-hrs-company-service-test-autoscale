package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "jobs_users")
public class JobUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer employmentTypeId;

    private Date startDate;

    private Date endDate;

    private Integer officeId;

    @OneToOne
    private Department department;

    @OneToOne
    private Company company;


}
