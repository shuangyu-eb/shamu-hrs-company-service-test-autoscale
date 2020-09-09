package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "compensation_change_reasons")
@NoArgsConstructor
public class CompensationChangeReason extends BaseEntity {

  @ManyToOne private Company company;

  private String name;
}
