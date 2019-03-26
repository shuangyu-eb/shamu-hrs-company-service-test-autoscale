package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "compensation_change_reasons")
@NoArgsConstructor
public class CompensationChangeReason extends BaseEntity{

    @ManyToOne
    private Company company;

    private String name;
}
