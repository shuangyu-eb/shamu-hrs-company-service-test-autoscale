package shamu.company.employee;

import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "employment_types")
@Where(clause = "deleted_at IS NULL")
public class EmploymentType extends BaseEntity {

    private String name;
}
