package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "user_compensations")
@NoArgsConstructor
public class UserCompensation extends BaseEntity {

    private Integer amount;

    private Integer frequency;

    private Integer currency;

    private Timestamp startDate;

    private Timestamp endDate;

    @OneToOne
    private User user;

    @OneToOne
    private CompensationType compensationType;

    @OneToOne
    private CompensationChangeReason compensationChangeReason;
}
