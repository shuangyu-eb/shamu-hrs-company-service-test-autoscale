package shamu.company.common.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Data
@Entity
@Table(name = "states_provinces")
@Where(clause = "deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
public class StateProvince extends BaseEntity {

  @ManyToOne
  private Country country;

  private String name;

  public StateProvince(Long id) {
    this.setId(id);
  }
}
