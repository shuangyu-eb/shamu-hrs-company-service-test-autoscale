package shamu.company.attendance.dto;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Data
public class OvertimeRuleDto extends BaseEntity {
  private static final long serialVersionUID = -2337367421284456113L;
  private Integer start;

  private Double rate;

  public void setStart(final Integer start) {
    this.start = start * 60;
  }
}
