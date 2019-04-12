package shamu.company.company.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "company_sizes")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class CompanySize extends BaseEntity {

  private String name;

  public CompanySize(String name) {
    this.name = name;
  }
}
