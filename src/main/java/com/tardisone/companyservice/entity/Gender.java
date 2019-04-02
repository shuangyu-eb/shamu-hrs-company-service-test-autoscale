package com.tardisone.companyservice.entity;

import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "genders")
@Where(clause = "deleted_at IS NULL")
public class Gender extends BaseEntity {

    private String name;
}
