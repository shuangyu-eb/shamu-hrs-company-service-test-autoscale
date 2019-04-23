package shamu.company.company.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.job.pojo.OfficeAddressPojo;

@Data
@Entity
@NoArgsConstructor
@Table(name = "office_addresses")
@Where(clause = "deleted_at IS NULL")
public class OfficeAddress extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Office office;

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

  public OfficeAddress(String street1, String street2, String city, String postalCode) {
    this.street1 = street1;
    this.street2 = street2;
    this.city = city;
    this.postalCode = postalCode;
  }

  public OfficeAddress(OfficeAddressPojo officeAddressPojo) {
    this.street1 = officeAddressPojo.getStreet1();
    this.street2 = officeAddressPojo.getStreet2();
    this.city = officeAddressPojo.getCity();
    this.postalCode = officeAddressPojo.getZip();
  }

}
