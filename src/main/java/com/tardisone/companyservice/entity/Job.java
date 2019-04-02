package com.tardisone.companyservice.entity;

import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "jobs")
@Where(clause = "deleted_at IS NULL")
public class Job extends BaseEntity {

    private String title;

    @ManyToOne
    private Department department;
}
