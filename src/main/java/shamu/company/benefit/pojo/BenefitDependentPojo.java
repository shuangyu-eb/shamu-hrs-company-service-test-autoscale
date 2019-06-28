package shamu.company.benefit.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import lombok.Data;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.DependentRelationship;
import shamu.company.common.entity.StateProvince;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.User;

@Data
public class BenefitDependentPojo {

  @HashidsFormat
  private Long id;

  private User employee;

  private DependentRelationship dependentRelationship;

  private String firstName;

  private String lastName;

  private String phoneHome;

  private String phoneWork;

  private String phoneMobile;

  private String email;

  private String middleName;

  private String city;

  private String street1;

  private String street2;

  private String postalCode;

  @JSONField(format = "yyyy-MM-dd")
  private Date birthDate;

  private String ssn;

  private Gender gender;

  private StateProvince state;


  public BenefitPlanDependent getBenefitDependent() {
    BenefitPlanDependent benefitPlanDependent = new BenefitPlanDependent();
    benefitPlanDependent.setEmployee(this.employee);
    if (this.gender.getId() == null) {
      this.setGender(null);
    } else {
      benefitPlanDependent.setGender(this.gender);
    }
    if (this.state.getId() == null) {
      this.setState(null);
    } else {
      benefitPlanDependent.setState(this.state);
    }
    if (this.dependentRelationship.getId() == null) {
      benefitPlanDependent.setDependentRelationship(null);
    } else {
      benefitPlanDependent.setDependentRelationship(this.dependentRelationship);
    }
    benefitPlanDependent.setPhoneHome(this.phoneHome);
    benefitPlanDependent.setFirstName(this.firstName);
    benefitPlanDependent.setMiddleName(this.middleName);
    benefitPlanDependent.setLastName(this.lastName);
    benefitPlanDependent.setEmail(this.email);
    benefitPlanDependent.setPostalCode(this.postalCode);
    benefitPlanDependent.setSsn(this.ssn);
    benefitPlanDependent.setCity(this.city);
    benefitPlanDependent.setStreet1(this.street1);
    benefitPlanDependent.setStreet2(this.street2);
    benefitPlanDependent.setBirthDate(this.birthDate);
    return benefitPlanDependent;
  }

  public BenefitPlanDependent getUpdatedDependent(BenefitPlanDependent origin) {
    if (this.gender.getId() == null) {
      origin.setGender(null);
    } else {
      origin.setGender(this.gender);
    }
    if (this.state.getId() == null) {
      this.setState(null);
    } else {
      origin.setState(this.state);
    }
    if (this.dependentRelationship.getId() == null) {
      origin.setDependentRelationship(null);
    } else {
      origin.setDependentRelationship(this.dependentRelationship);
    }
    origin.setDependentRelationship(this.dependentRelationship);
    origin.setPhoneHome(this.phoneHome);
    origin.setFirstName(this.firstName);
    origin.setMiddleName(this.middleName);
    origin.setLastName(this.lastName);
    origin.setEmail(this.email);
    origin.setGender(this.gender);
    origin.setState(this.state);
    origin.setPostalCode(this.postalCode);
    origin.setSsn(this.ssn);
    origin.setCity(this.city);
    origin.setStreet1(this.street1);
    origin.setStreet2(this.street2);
    origin.setBirthDate(this.birthDate);
    return origin;
  }

}
