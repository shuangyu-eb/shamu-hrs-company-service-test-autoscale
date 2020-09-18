package shamu.company.company.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.validator.constraints.Length;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.StateProvince;

@Data
@Entity
@NoArgsConstructor
@Table(name = "office_addresses")
public class OfficeAddress extends BaseEntity {

  private static final long serialVersionUID = 5382370365272870248L;
  @Column(name = "street_1")
  @Length(max = 255)
  private String street1;

  @Column(name = "street_2")
  @Length(max = 255)
  private String street2;

  @Length(max = 100)
  private String city;

  @ManyToOne
  @NotFound(action = NotFoundAction.IGNORE)
  private StateProvince stateProvince;

  @Length(max = 30)
  private String postalCode;

  @OneToOne
  private StaticTimezone timeZone;
}
