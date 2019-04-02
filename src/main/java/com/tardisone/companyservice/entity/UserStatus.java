package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "user_statuses")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class UserStatus extends BaseEntity {

    public UserStatus(String name) {
        this.name = name;
    }

    private String name;
}
