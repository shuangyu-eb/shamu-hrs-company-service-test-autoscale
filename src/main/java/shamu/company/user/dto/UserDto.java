package shamu.company.user.dto;

import lombok.Data;

@Data
public class UserDto {

  private String id;

  private String firstName;

  private String lastName;

  private String email;

  private String avatar;

  private String salt;
}
