package shamu.company.user.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
@NoArgsConstructor
public class UserAddress extends BaseEntity {

  @OneToOne
  private User user;

  @Column(name = "street_1")
  private String street1;

  @Column(name = "street_2")
  private String street2;

  private String city;

  @ManyToOne
  private StateProvince stateProvince;

  @ManyToOne
  private Country country;

  private String postalCode;

  public UserAddress(User user) {
    this.user = user;
  }
}
