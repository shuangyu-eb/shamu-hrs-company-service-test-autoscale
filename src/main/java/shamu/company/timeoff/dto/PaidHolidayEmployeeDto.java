package shamu.company.timeoff.dto;

import lombok.Data;

@Data
public class PaidHolidayEmployeeDto implements UserIdDto {
  private String id;


  @Override
  public String getUserId() {
    return getId();
  }
}
