package shamu.company.company.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.Country;

@Data
@Entity
@Table(name = "companies")
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Where(clause = "deleted_at IS NULL")
public class Company extends BaseEntity {

  @Length(max = 255)
  private String name;

  private String imageUrl;

  @Column(name = "EIN")
  private String ein;

  @ManyToOne
  private CompanySize companySize;

  @ManyToOne
  private Country country;

  public Company(Long id) {
    this.setId(id);
  }
}
