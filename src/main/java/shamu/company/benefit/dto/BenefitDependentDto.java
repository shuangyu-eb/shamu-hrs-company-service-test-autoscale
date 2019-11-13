package shamu.company.benefit.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import lombok.Data;
import shamu.company.crypto.Crypto;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.hashids.HashidsFormat;

@Data
public class BenefitDependentDto {

  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long employeeId;

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

  @JSONField(format = "yyyy-MM-dd")
  private Date birthDate;

  private SelectFieldInformationDto gender;

  private SelectFieldInformationDto state;

  private SelectFieldInformationDto relationShip;
}
