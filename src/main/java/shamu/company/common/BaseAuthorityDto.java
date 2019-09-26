package shamu.company.common;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class BaseAuthorityDto {

  @HashidsFormat
  private Long id;
}
