package shamu.company.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;

@Entity
@Data
@Table(name = "user_addresses")
@NoArgsConstructor
public class UserAddress extends BaseEntity {

  @OneToOne private User user;

  @Column(name = "street_1")
  @Length(max = 255)
  private String street1;

  @Column(name = "street_2")
  @Length(max = 255)
  private String street2;

  @Length(max = 100)
  private String city;

  @ManyToOne private StateProvince stateProvince;

  @ManyToOne private Country country;

  @Length(max = 30)
  private String postalCode;

  public UserAddress(final User user) {
    this.user = user;
  }
}
