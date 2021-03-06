package shamu.company.benefit.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.StateProvince;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "user_dependents")
@NoArgsConstructor
public class BenefitPlanDependent extends BaseEntity {

  private static final long serialVersionUID = -4190925451913461606L;
  @ManyToOne private User employee;

  @ManyToOne private DependentRelationship dependentRelationship;

  @Length(max = 100)
  private String firstName;

  @Length(max = 100)
  private String lastName;

  @Length(max = 255)
  private String phoneHome;

  @Length(max = 255)
  private String phoneWork;

  @Length(max = 50)
  private String phoneMobile;

  @Length(max = 255)
  private String email;

  @Length(max = 100)
  private String middleName;

  @Length(max = 100)
  private String city;

  @Column(name = "street_1")
  @Length(max = 255)
  private String street1;

  @Column(name = "street_2")
  @Length(max = 255)
  private String street2;

  @Length(max = 30)
  private String postalCode;

  @JsonFormat(pattern = "MM/dd/yyyy")
  private Date birthDate;

  private String ssn;

  @ManyToOne private Gender gender;

  @ManyToOne private StateProvince state;
}
