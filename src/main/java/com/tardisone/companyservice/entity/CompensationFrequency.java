package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Data
@Entity
public class CompensationFrequency extends BaseEntity {

    @ManyToOne
    private Company company;

    private String name;

}
