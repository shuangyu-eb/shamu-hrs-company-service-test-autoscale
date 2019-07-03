package shamu.company.company.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.Country;

@Data
@Entity
@Table(name = "companies")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class Company extends BaseEntity {

  private String name;

  private String imageUrl;

  @Column(name = "EIN")
  private String ein;

  @ManyToOne
  private CompanySize companySize;

  @ManyToOne
  private Country country;

  private String subdomainName;

  public Company(Long id) {
    this.setId(id);
  }
}
