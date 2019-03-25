package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "user_statuses")
public class UserStatus {

    @Id
    private Long id;

    private String name;
}
