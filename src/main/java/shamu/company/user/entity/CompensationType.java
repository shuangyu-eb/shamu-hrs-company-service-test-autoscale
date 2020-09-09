package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "compensation_types")
@NoArgsConstructor
public class CompensationType extends BaseEntity {

  private static final long serialVersionUID = -4194441952242622443L;
  private String name;

  public CompensationType(final String compensationId) {
    setId(compensationId);
  }
}
