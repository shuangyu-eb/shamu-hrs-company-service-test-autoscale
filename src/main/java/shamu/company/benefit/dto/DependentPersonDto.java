package shamu.company.benefit.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import lombok.Data;

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
}
