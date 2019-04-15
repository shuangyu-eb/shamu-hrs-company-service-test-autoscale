package shamu.company.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;

@Entity
@Data
@Table(name = "user_addresses")
@Where(clause = "deleted_at IS NULL")
@AllArgsConstructor
@NoArgsConstructor
public class UserAddress extends BaseEntity {

  @OneToOne private User user;

  @Column(name = "street_1")
  private String street1;

  @Column(name = "street_2")
  private String street2;

  private String city;

  @ManyToOne private StateProvince stateProvince;

  @ManyToOne private Country country;

  private String postalCode;

  public UserAddress(
      Long id,
      Long userId,
      String street1,
      String street2,
      String city,
      Long stateProvinceId,
      Country country,
      String postalCode) {
    this.setId(id);
    this.setUser(new User(userId));
    this.setStreet1(street1);
    this.setStreet2(street2);
    this.setCity(city);
    this.setStateProvince(new StateProvince(stateProvinceId));
    this.setCountry(country);
    this.setPostalCode(postalCode);
  }
}
