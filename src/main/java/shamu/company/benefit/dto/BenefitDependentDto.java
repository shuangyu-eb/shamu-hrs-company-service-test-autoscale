package shamu.company.benefit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.sql.Date;
import lombok.Data;
import shamu.company.crypto.CryptoSsnSerializer;
import shamu.company.employee.dto.SelectFieldInformationDto;

@Data
public class BenefitDependentDto {

  private String id;

  private String employeeId;

  private String firstName;

  private String middleName;

  private String lastName;

  private String email;

  @JsonSerialize(using = CryptoSsnSerializer.class)
  private String ssn;

  private String phone;

  private String street1;

  private String street2;

  private String postalCode;

  private String city;

  @JsonFormat(pattern = "MM/dd/yyyy")
  private Date birthDate;

  private SelectFieldInformationDto gender;

  private SelectFieldInformationDto state;

  private SelectFieldInformationDto relationShip;
}
