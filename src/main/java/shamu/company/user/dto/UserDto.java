package shamu.company.user.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.s3.PreSinged;

@Data
public class UserDto {

  @HashidsFormat
  private Long id;

  private String name;

  private String email;

  @PreSinged
  private String avatar;
}
