package shamu.company.employee.dto;

import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.dto.JobUserDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRelatedInformationDto {

  @HashidsFormat
  private Long userId;

  private String userStatus;

  private Timestamp emailSendDate;

  private String employeeFirstName;

  private String employeeLastName;

  private String employeeImageUrl;

  private String employeeWorkPhone;

  private String employeeWorkEmail;

  private String employeeJobTitle;

  @HashidsFormat
  private Long managerId;

  private String managerFirstName;

  private String managerLastName;

  private String managerImageUrl;

  private String managerJobTitle;

  private List<JobUserDto> directReporters;
}
