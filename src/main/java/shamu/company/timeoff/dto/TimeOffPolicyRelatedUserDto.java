package shamu.company.timeoff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.dto.JobUserDto;

@Data
@AllArgsConstructor
public class TimeOffPolicyRelatedUserDto {

  private JobUserDto jobUserDto;

  private Integer balance;

  private String department;

  private String employmentType;

  private String firstName;

  @HashidsFormat
  private Long id;

  private String imageUrl;

  private String jobTitle;

  private String lastName;

  public TimeOffPolicyRelatedUserDto(Integer balcance,JobUserDto jobUserDto) {
    this.balance = balcance;
    this.department = jobUserDto.getDepartment() == null ? null : jobUserDto.getDepartment();
    this.employmentType = jobUserDto.getEmploymentType();
    this.jobTitle = jobUserDto.getJobTitle();
    this.firstName = jobUserDto.getFirstName();
    this.lastName = jobUserDto.getLastName();
    this.id = jobUserDto.getId();
    this.imageUrl = jobUserDto.getImageUrl();
  }
}
