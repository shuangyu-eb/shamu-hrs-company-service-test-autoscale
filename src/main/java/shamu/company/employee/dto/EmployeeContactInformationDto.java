package shamu.company.employee.dto;

import lombok.Data;
import shamu.company.user.dto.BasicUserContactInformationDto;

@Data
public class EmployeeContactInformationDto extends BasicUserContactInformationDto {

  private String phoneHome;

  private String emailHome;
}
