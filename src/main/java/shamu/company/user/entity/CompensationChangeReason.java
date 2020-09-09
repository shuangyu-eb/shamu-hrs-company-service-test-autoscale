package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "compensation_change_reasons")
@NoArgsConstructor
public class CompensationChangeReason extends BaseEntity {

  private static final long serialVersionUID = -1677557002329594088L;
  private String name;
}
