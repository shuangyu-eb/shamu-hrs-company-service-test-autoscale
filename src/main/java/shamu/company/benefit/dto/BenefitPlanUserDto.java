package shamu.company.benefit.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.User;

@Data
public class BenefitPlanUserDto {
  @HashidsFormat
  private Long id;

  private String firstName;

  private String lastName;

  private String imageUrl;

  public BenefitPlanUserDto(User user) {
    this.id = user.getId();
    this.firstName = user.getUserPersonalInformation().getFirstName();
    this.lastName = user.getUserPersonalInformation().getLastName();
    this.imageUrl = user.getImageUrl();
  }
}
