package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "compensation_change_reasons")
public class CompensationChangeReason {

    @Id
    private Long id;

    @ManyToOne
    private Company company;

    private String name;
}
