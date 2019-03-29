package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "martial_status")
public class MaritalStatus extends BaseEntity {

    private String name;
}
