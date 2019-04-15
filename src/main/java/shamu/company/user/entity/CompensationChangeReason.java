package shamu.company.user.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "compensation_change_reasons")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class CompensationChangeReason extends BaseEntity {

    @ManyToOne
    private Company company;

    private String name;
}
