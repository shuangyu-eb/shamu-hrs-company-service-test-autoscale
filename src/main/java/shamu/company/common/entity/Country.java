package shamu.company.common.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Data
@Entity
@Table(name = "countries")
@Where(clause = "deleted_at IS NULL")
@NoArgsConstructor
public class Country extends BaseEntity {

  private String name;

  public Country(Long id){
    this.setId(id);
  }
}
