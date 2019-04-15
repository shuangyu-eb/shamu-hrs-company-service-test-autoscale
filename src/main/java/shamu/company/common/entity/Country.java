package shamu.company.common.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;

@Data
@Entity
@Table(name = "countries")
@Where(clause = "deleted_at IS NULL")
public class Country extends BaseEntity {

  private String name;
}
