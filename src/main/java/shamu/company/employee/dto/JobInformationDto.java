package shamu.company.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.user.entity.UserCompensation;

@Data
public class JobInformationDto extends BasicJobInformationDto {

  private CompensationDto compensation;
}
