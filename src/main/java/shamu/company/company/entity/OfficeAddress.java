package shamu.company.company.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;

@Data
@Entity
@NoArgsConstructor
@Table(name = "office_addresses")
@Where(clause = "deleted_at IS NULL")
public class OfficeAddress extends BaseEntity {

  @Column(name = "street_1")
  @Length(max = 255)
  private String street1;

  @Column(name = "street_2")
  @Length(max = 255)
  private String street2;

  @Length(max = 100)
  private String city;

  @ManyToOne
  private StateProvince stateProvince;

  // TODO remove it, we can get it from StateProvince
  @ManyToOne
  private Country country;

  @Length(max = 30)
  private String postalCode;

}
