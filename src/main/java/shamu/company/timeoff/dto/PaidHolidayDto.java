package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.BaseAuthorityDto;
import shamu.company.common.validation.constraints.YesterdayOrLater;

@Data
@NoArgsConstructor
public class PaidHolidayDto extends BaseAuthorityDto {

  private String name;

  private String nameShow;

  @YesterdayOrLater
  private Timestamp date;

  private Boolean isSelected;

  private Boolean federal;

  private Boolean editable;

}
