package shamu.company.company.entity;

import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "offices")
@Where(clause = "deleted_at IS NULL")
public class Office extends BaseEntity {

    @ManyToOne
    private Company company;

    private String officeId;

    private String name;

    private String phone;

    private String email;

    @OneToOne
    private OfficeAddress officeAddress;
}
