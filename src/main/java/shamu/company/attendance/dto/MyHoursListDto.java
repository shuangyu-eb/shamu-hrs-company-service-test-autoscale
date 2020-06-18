package shamu.company.attendance.dto;

import java.util.List;
import lombok.Data;

@Data
public class MyHoursListDto {
  private String date;

  private List<MyHoursEntryDto> myHoursEntryDtos;
}
