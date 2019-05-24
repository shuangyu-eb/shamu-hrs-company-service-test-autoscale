package shamu.company.timeoff.dto;

import lombok.Data;

@Data
public class TimeOffBalanceDto {

  private Long id;

  private String name;

  private Integer balance;
}
