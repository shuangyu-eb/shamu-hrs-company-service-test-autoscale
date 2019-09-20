package shamu.company.admin.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.User;

@Data
public class SuperAdminUserDto {

  @HashidsFormat
  private Long userId;

  private String imageUrl;

  private String firstName;

  private String lastName;

  private String company;

  private String role;

  private String email;

  public SuperAdminUserDto(final User user) {
    this.userId = user.getId();
    this.imageUrl = user.getImageUrl();
    this.firstName = user.getUserPersonalInformation().getFirstName();
    this.lastName = user.getUserPersonalInformation().getLastName();
    this.email = user.getUserContactInformation().getEmailWork();
    this.company = user.getCompany().getName();
  }
}
