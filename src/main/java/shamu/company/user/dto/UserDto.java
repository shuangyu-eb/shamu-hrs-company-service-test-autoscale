package shamu.company.user.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class UserDto {

  @HashidsFormat
  private Long id;

  private String name;

  private String email;

  private String avatar;
}
