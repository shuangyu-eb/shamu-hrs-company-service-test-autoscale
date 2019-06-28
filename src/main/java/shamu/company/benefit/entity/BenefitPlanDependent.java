package shamu.company.benefit.entity;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.StateProvince;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "benefit_plan_dependents")
@Where(clause = "deleted_at IS NULL")
@NoArgsConstructor
public class BenefitPlanDependent extends BaseEntity {

  @ManyToOne
  private User employee;

  @ManyToOne
  private DependentRelationship dependentRelationship;

  private String firstName;

  private String lastName;

  private String phoneHome;

  private String phoneWork;

  private String phoneMobile;

  private String email;

  private String middleName;

  private String city;

  @Column(name = "street_1")
  private String street1;

  @Column(name = "street_2")
  private String street2;

  private String postalCode;

  @JSONField(format = "yyyy-MM-dd")
  private Date birthDate;

  private String ssn;

  @ManyToOne
  private Gender gender;

  @ManyToOne
  private StateProvince state;


}
