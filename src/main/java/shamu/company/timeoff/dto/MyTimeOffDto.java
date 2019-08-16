package shamu.company.timeoff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageImpl;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyTimeOffDto {

  private Boolean policiesAdded;

  private PageImpl<TimeOffRequestDto> timeOffRequests;
}
