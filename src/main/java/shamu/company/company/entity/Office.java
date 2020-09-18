package shamu.company.company.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;

@Data
@NoArgsConstructor
@Entity
@Table(name = "offices")
public class Office extends BaseEntity {

  private static final long serialVersionUID = 4055344587456808308L;
  private String officeId;

  @Length(max = 100)
  private String name;

  @Length(max = 50)
  private String phone;

  @Length(max = 255)
  private String email;

  @OneToOne(
      orphanRemoval = true,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
  private OfficeAddress officeAddress;

  public Office(final String name, final OfficeAddress officeAddress) {
    this.name = name;
    this.officeAddress = officeAddress;
  }
}
