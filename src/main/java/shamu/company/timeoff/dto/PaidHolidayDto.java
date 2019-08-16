package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import javax.validation.constraints.FutureOrPresent;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;

@Data
@NoArgsConstructor
public class PaidHolidayDto {

  @HashidsFormat
  private Long id;

  private String name;

  private String nameShow;

  @FutureOrPresent
  private Timestamp date;

  private Boolean isSelected;
}
