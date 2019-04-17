package com.tardisone.companyservice.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "jobs_users")
public class JobUser extends BaseEntity  {


    @ManyToOne
    private EmploymentType employmentType;

    @OneToOne
    private User user;

    @OneToOne
    private Job job;

    private Date startDate;

    private Date endDate;

    @ManyToOne
    private Office office;

    @ManyToOne
    private Department department;

    @ManyToOne
    private Company company;

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

}
