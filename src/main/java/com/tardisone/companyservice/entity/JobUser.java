package com.tardisone.companyservice.entity;

import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "jobs_users")
@Data
@Where(clause = "deleted_at IS NULL")
public class JobUser extends BaseEntity {

    @OneToOne
    private User user;

    @OneToOne
    private Job job;

    @ManyToOne
    private EmploymentType employmentType;

    private Timestamp startDate;

    private Timestamp endDate;

    @ManyToOne
    private Office office;

    @ManyToOne
    private Department department;

    @ManyToOne
    private Company company;

}
