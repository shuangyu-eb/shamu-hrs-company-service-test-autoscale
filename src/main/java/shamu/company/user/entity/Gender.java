package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "genders")
@NoArgsConstructor
public class Gender extends BaseEntity {

  private String name;

  public Gender(final String id) {
    setId(id);
  }
}
