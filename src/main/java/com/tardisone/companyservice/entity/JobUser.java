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
    private Long id;

    @ManyToOne
    private EmploymentType employmentType;

    private Long userId;

    private Long jobId;

    private Date startDate;

    private Date endDate;

    @OneToOne
    private Office office;

    @OneToOne
    private Department department;

    @OneToOne
    private Company company;

    @OneToOne
    @JoinColumn(name = "id")
    private User managerId;





}
