package shamu.company.attendance.dto;

import java.util.List;
import lombok.Data;

@Data
public class MyHoursEntryDto {
  private String entryId;

  private String comments;

  List<MyHoursTimeLogDto> myHoursTimeLogDtos;
}
