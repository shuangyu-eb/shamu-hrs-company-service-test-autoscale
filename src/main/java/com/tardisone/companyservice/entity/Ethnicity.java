package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "ethnicities")
public class Ethnicity extends BaseEntity {

    private String name;
}
