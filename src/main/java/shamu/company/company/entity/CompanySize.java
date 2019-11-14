package shamu.company.company.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "company_sizes")
@NoArgsConstructor
public class CompanySize extends BaseEntity {


  private String name;

  public CompanySize(final String id) {
    this.setId(id);
  }
}
