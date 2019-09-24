package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.validation.constraints.YesterdayOrLater;
import shamu.company.hashids.HashidsFormat;

@Data
@NoArgsConstructor
public class PaidHolidayDto {

  @HashidsFormat
  private Long id;

  private String name;

  private String nameShow;

  @YesterdayOrLater
  private Timestamp date;

  private Boolean isSelected;

  private Boolean federal;

  private Boolean editable;

}
