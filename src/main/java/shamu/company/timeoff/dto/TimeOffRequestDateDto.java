package shamu.company.timeoff.dto;

import java.sql.Timestamp;

public interface TimeOffRequestDateDto {
  Timestamp getDate();

  Integer getHours();
}