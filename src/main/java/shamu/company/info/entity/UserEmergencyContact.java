package shamu.company.info.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.StateProvince;
import shamu.company.user.entity.User;

@Entity
@Table(name = "user_emergency_contacts")
@Data
@NoArgsConstructor
public class UserEmergencyContact extends BaseEntity {

  @ManyToOne
  private User user;

  @Length(max = 100)
  private String firstName;

  @Length(max = 100)
  private String lastName;

  @Length(max = 30)
  private String relationship;

  @Length(max = 50)
  private String phone;

  @Length(max = 255)
  private String email;

  @Column(name = "street_1")
  @Length(max = 255)
  private String street1;

  @Length(max = 255)
  @Column(name = "street_2")
  private String street2;

  @Length(max = 100)
  private String city;

  @ManyToOne
  private StateProvince state;

  @Length(max = 30)
  private String postalCode;

  private Boolean isPrimary = false;
}
