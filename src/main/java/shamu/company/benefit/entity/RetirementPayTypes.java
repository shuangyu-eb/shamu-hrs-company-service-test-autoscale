package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "static_retirement_pay_types")
@NoArgsConstructor
public class RetirementPayTypes extends BaseEntity {

  private static final long serialVersionUID = 2646834505716094521L;

  private String name;

  public RetirementPayTypes(final String id) {
    setId(id);
  }

}
