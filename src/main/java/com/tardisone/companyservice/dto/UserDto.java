package com.tardisone.companyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto  {
  private Long userId;

  private String employeeNumber;

  private String emailWork;

  private String jobTitle;

  private String location;

  private String phone;

  private String password;

  private String imageUrl;

}
