package shamu.company.employee.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.job.JobUserDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRelatedInformationDto {

  private Long userId;

  private String employeeFirstName;

  private String employeeImageUrl;

  private String employeeWorkPhone;

  private String employeeWorkEmail;

  private String employeeJobTitle;

  private String managerName;

  private String managerImageUrl;

  private String managerJobTitle;

  private List<JobUserDto> directReporters;

  public EmployeeRelatedInformationDto(Long userId, JobUserDto jobEmployeeDto,
      JobUserDto jobManagerDto, List<JobUserDto> directReporters) {
    this.setUserId(userId);
    this.setEmployeeFirstName(jobEmployeeDto.getFirstName());
    this.setEmployeeImageUrl(jobEmployeeDto.getImageUrl());
    this.setEmployeeWorkEmail(jobEmployeeDto.getEmail());
    this.setEmployeeWorkPhone(jobEmployeeDto.getPhoneNumber());
    this.setEmployeeJobTitle(jobEmployeeDto.getJobTitle());
    this.setManagerName(jobManagerDto.getFirstName());
    this.setManagerImageUrl(jobManagerDto.getImageUrl());
    this.setManagerJobTitle(jobManagerDto.getJobTitle());
    this.setDirectReporters(directReporters);
  }


}
