package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "ethnicities")
@Where(clause = "deleted_at IS NULL")
@NoArgsConstructor
public class Ethnicity extends BaseEntity {

  private String name;

  public Ethnicity(Long id) {
    this.setId(id);
  }
}
