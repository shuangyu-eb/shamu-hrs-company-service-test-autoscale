package com.tardisone.companyservice.entity;

import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "departments")
@Where(clause = "deleted_at IS NULL")
public class Department extends BaseEntity {

    @ManyToOne
    private Company company;

    private String name;
}
