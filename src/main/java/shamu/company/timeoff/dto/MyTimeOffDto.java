package shamu.company.timeoff.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyTimeOffDto {

  private Boolean policiesAdded;

  private List<TimeOffRequestDto> timeOffRequestDtos;
}
