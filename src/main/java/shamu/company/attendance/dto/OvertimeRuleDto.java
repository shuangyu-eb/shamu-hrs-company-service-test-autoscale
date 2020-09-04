package shamu.company.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeRuleDto extends BaseEntity {
  private static final long serialVersionUID = -2337367421284456113L;
  private Integer start;

  private Double rate;
}
