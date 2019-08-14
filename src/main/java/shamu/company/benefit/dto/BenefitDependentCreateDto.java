package shamu.company.benefit.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import lombok.Data;
import shamu.company.benefit.entity.DependentRelationship;
import shamu.company.common.entity.StateProvince;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.User;

@Data
public class BenefitDependentCreateDto {

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
}
