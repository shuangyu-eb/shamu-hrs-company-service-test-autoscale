package shamu.company.timeoff.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class UnimplementedRequestDto {

  @HashidsFormat private Long userId;

  private Integer hours;
}
