package shamu.company.company.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;

@Data
@NoArgsConstructor
@Entity
@Table(name = "offices")
public class Office extends BaseEntity {

  @ManyToOne
  private Company company;

  private String officeId;

  @Length(max = 100)
  private String name;

  @Length(max = 50)
  private String phone;

  @Length(max = 255)
  private String email;

  @OneToOne(orphanRemoval = true,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
  private OfficeAddress officeAddress;

  public Office(final String name, final OfficeAddress officeAddress, final Company company) {
    this.name = name;
    this.officeAddress = officeAddress;
    this.company = company;
  }
}
