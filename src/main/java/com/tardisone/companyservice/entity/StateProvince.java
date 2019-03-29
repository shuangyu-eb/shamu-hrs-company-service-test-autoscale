package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "states_provinces")
public class StateProvince extends BaseEntity {

    @ManyToOne
    private Country country;

    private String name;
}
