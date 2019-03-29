package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "citizenship_statuses")
public class CitizenshipStatus extends BaseEntity {
    private String name;
}
