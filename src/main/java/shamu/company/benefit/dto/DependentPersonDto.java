package shamu.company.benefit.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import lombok.Data;
import shamu.company.benefit.entity.BenefitPlanDependent;

@Data
public class DependentPersonDto {

  private String firstName;

  private String middleName;

  private String lastName;

  private String email;

  private String ssn;

  private String phone;

  private String street1;

  private String street2;

  private String postalCode;

  private String city;

  @JSONField(format = "yyyy-MM-dd")
  private Date birthDate;

  public DependentPersonDto(BenefitPlanDependent benefitPlanDependent) {
    this.firstName = benefitPlanDependent.getFirstName();
    this.middleName = benefitPlanDependent.getMiddleName();
    this.birthDate = benefitPlanDependent.getBirthDate();
    this.email = benefitPlanDependent.getEmail();
    this.phone = benefitPlanDependent.getPhoneHome();
    this.ssn = benefitPlanDependent.getSsn();
    this.city = benefitPlanDependent.getCity();
    this.postalCode = benefitPlanDependent.getPostalCode();
    this.street1 = benefitPlanDependent.getStreet1();
    this.street2 = benefitPlanDependent.getStreet2();
  }

}
