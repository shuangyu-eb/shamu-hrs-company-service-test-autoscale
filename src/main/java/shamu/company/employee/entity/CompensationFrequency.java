package shamu.company.employee.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Data
@Entity
public class CompensationFrequency extends BaseEntity {

    @ManyToOne
    private Company company;

    private String name;

}
