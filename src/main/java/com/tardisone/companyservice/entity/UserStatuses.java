package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "user_statuses")
@NoArgsConstructor
public class UserStatuses {

    @Id
    private Long id;

    private  String name;


}
