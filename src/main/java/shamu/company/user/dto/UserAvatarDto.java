package shamu.company.user.dto;

import lombok.Data;
import shamu.company.s3.PreSinged;

@Data
public class UserAvatarDto {

  @PreSinged
  private String userAvatar;

  @PreSinged
  private String managerAvatar;
}
