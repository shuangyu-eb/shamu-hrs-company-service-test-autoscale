package shamu.company.company.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@NoArgsConstructor
@Entity
@Table(name = "offices")
@Where(clause = "deleted_at IS NULL")
public class Office extends BaseEntity {

  @ManyToOne
  private Company company;

  private String officeId;

  private String name;

  private String phone;

  private String email;

  @OneToOne
  private OfficeAddress officeAddress;

  public Office(String name, OfficeAddress officeAddress, Company company) {
    this.name = name;
    this.officeAddress = officeAddress;
    this.company = company;
  }
}