package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "user_compensations")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class UserCompensation extends BaseEntity {

    private Integer wage;

    private Timestamp startDate;

    private Timestamp endDate;

    private String overtimeStatus;

    @OneToOne
    private User user;

    @OneToOne
    private CompensationType compensationType;

    @OneToOne
    private CompensationChangeReason compensationChangeReason;

    private String comment;
}
