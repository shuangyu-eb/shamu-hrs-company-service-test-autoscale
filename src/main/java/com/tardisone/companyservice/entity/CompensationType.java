package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "compensation_types")
@NoArgsConstructor
public class CompensationType extends BaseEntity {

    @ManyToOne
    private Company company;

    private String name;
}
