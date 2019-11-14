package shamu.company.common.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "states_provinces")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StateProvince extends BaseEntity {

  @ManyToOne
  private Country country;

  private String name;

  public StateProvince(final String id) {
    setId(id);
  }
}
