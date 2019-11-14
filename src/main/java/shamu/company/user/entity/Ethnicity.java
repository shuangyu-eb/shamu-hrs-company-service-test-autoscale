package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "ethnicities")
@NoArgsConstructor
public class Ethnicity extends BaseEntity {

  private String name;

  public Ethnicity(final String id) {
    setId(id);
  }
}
