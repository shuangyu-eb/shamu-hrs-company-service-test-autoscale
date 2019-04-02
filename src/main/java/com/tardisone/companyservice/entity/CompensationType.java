package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "compensation_types")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class CompensationType extends BaseEntity {

    @ManyToOne
    private Company company;

    private String name;
}
