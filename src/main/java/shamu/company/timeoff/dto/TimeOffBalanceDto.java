package shamu.company.timeoff.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class TimeOffBalanceDto {

  @HashidsFormat
  private Long id;

  private String name;

  private Integer balance;
}
