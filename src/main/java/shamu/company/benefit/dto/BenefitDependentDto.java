package shamu.company.benefit.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import lombok.Data;
import shamu.company.crypto.Crypto;
import shamu.company.employee.dto.SelectFieldInformationDto;

@Data
public class BenefitDependentDto {

  private String id;

  private String employeeId;

  private String firstName;

  private String middleName;

  private String lastName;

  private String email;

  @Crypto(field = "employeeId")
  private String ssn;

  private String phone;

  private String street1;

  private String street2;

  private String postalCode;

  private String city;

  @JSONField(format = "MM/dd/yyyy")
  private Date birthDate;

  private SelectFieldInformationDto gender;

  private SelectFieldInformationDto state;

  private SelectFieldInformationDto relationShip;
}
