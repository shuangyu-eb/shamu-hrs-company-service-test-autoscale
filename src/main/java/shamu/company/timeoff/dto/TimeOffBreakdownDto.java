package shamu.company.timeoff.dto;

import java.util.List;
import lombok.Data;

@Data
public class TimeOffBreakdownDto {

  private Integer balance;

  private Long untilDateInMillis;

  private List<TimeOffBreakdownItemDto> list;
}
