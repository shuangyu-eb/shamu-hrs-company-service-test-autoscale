package com.tardisone.companyservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "company_sizes")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class CompanySize extends BaseEntity{

    public CompanySize(String name) {
        this.name = name;
    }

    private String name;
}
