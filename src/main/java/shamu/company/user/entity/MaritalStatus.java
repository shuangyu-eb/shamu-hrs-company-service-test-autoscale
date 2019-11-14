package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "marital_status")
@NoArgsConstructor
public class MaritalStatus extends BaseEntity {

  private String name;

  public MaritalStatus(final String id) {
    setId(id);
  }
}
