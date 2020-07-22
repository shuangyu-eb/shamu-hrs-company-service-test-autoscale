package shamu.company.attendance.dto;

import lombok.Data;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.user.entity.User;

@Data
public class EmployeesTaSettingDto {

  private User employee;

  private StaticTimezone timeZone;

  private int messagingOn;

}
