package shamu.company.attendance.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StaticTimezoneDto {
  private String id;

  private String name;

  private String abbreviation;

}
