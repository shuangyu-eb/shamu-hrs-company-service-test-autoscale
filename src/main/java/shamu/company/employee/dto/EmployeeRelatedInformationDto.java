package shamu.company.employee.dto;

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

  public EmployeeRelatedInformationDto(
      Long userId,
      JobUserDto jobEmployeeDto,
      JobUserDto jobManagerDto,
      List<JobUserDto> directReporters) {
    this.setUserId(userId);
    this.setEmployeeFirstName(jobEmployeeDto.getFirstName());
    this.setEmployeeLastName(jobEmployeeDto.getLastName());
    this.setEmployeeImageUrl(jobEmployeeDto.getImageUrl());
    this.setEmployeeWorkEmail(jobEmployeeDto.getEmail());
    this.setEmployeeWorkPhone(jobEmployeeDto.getPhoneNumber());
    this.setEmployeeJobTitle(jobEmployeeDto.getJobTitle());
    if (jobManagerDto != null) {
      this.setManagerId(jobManagerDto.getId());
      this.setManagerFirstName(jobManagerDto.getFirstName());
      this.setManagerLastName(jobManagerDto.getLastName());
      this.setManagerImageUrl(jobManagerDto.getImageUrl());
      this.setManagerJobTitle(jobManagerDto.getJobTitle());
    }
    this.setDirectReporters(directReporters);
  }
}
