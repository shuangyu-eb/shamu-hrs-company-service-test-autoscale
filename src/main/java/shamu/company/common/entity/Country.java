package shamu.company.common.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "countries")
@NoArgsConstructor
public class Country extends BaseEntity {

  private String name;

  public Country(final String id) {
    setId(id);
  }
}
