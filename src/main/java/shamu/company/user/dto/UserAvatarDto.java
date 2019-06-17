package shamu.company.user.dto;

import lombok.Data;
import shamu.company.user.entity.User;

@Data
public class UserAvatarDto {

  private String userAvatar;

  private String managerAvatar;

  public UserAvatarDto(User user) {
    setUserAvatar(user.getImageUrl());

    User manager = user.getManagerUser();
    setManagerAvatar(manager != null ? manager.getImageUrl() : user.getImageUrl());
  }
}
