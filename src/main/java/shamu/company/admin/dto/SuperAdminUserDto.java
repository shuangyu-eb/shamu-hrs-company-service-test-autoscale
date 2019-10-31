package shamu.company.admin.dto;

import lombok.Data;
import lombok.ToString;
import shamu.company.hashids.HashidsFormat;
import shamu.company.s3.PreSinged;
import shamu.company.user.entity.User;

@Data
public class SuperAdminUserDto {

  @HashidsFormat
  private Long userId;

  @PreSinged
  private String imageUrl;

  private String firstName;

  private String lastName;

  private String company;

  private String role;

  private String email;

  @ToString.Exclude
  private String auth0UserId;

  public SuperAdminUserDto(final User user) {
    userId = user.getId();
    imageUrl = user.getImageUrl();
    firstName = user.getUserPersonalInformation().getFirstName();
    lastName = user.getUserPersonalInformation().getLastName();
    email = user.getUserContactInformation().getEmailWork();
    company = user.getCompany().getName();
    auth0UserId = user.getUserId();
    role = user.getRole().getValue();
  }
}
