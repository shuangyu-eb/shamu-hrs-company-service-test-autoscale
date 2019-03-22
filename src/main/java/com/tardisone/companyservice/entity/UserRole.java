package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "user_roles")
public class UserRole {

    @Id
    private Long id;

    private String name;
}
