package shamu.company.benefit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.sql.Date;
import lombok.Data;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.DependentRelationship;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.validation.constraints.SsnValidate;
import shamu.company.crypto.Crypto;
import shamu.company.crypto.CryptoSsnSerializer;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.User;

@Data
public class BenefitDependentCreateDto {

  private String id;

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

  @JsonFormat(pattern = "MM/dd/yyyy")
  private Date birthDate;

  @Crypto(field = "id", targetType = BenefitPlanDependent.class)
  @SsnValidate
  @JsonSerialize(using = CryptoSsnSerializer.class)
  private String ssn;

  private Gender gender;

  private StateProvince state;
}
