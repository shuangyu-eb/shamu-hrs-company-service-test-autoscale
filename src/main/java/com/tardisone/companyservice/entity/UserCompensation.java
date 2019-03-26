package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "user_compensations")
public class UserCompensation {

    @Id
    private Long id;

    private Integer amount;

    private Integer frequency;

    private Integer currency;

    private Timestamp startDate;

    private Timestamp endDate;

}
