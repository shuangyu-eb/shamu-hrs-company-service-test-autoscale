package shamu.company.admin.dto;

import lombok.Data;
import lombok.ToString;
import shamu.company.user.entity.User;

@Data
public class SuperAdminUserDto {

  private String userId;

  private String imageUrl;

  private String firstName;

  private String preferredName;

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
    preferredName = user.getUserPersonalInformation().getPreferredName();
    email = user.getUserContactInformation().getEmailWork();
    company = user.getCompany().getName();
    auth0UserId = user.getId();
    role = user.getRole().getValue();
  }
}
