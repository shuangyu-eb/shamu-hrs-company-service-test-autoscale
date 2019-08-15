package shamu.company.job.entity.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.common.mapper.Config;
import shamu.company.common.mapper.SelectFieldInformationDtoUtils;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.EmployeeRelatedInformationDto;
import shamu.company.employee.dto.JobInformationDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;

@Mapper(
    config = Config.class,
    uses = {SelectFieldInformationDtoUtils.class, OfficeMapper.class}
)
public interface JobUserMapper {

  @Mapping(target = "department", source = "job.department")
  @Mapping(target = "manager", source = "user.managerUser")
  @Mapping(target = "userRole", source = "user.role")
  @Mapping(target = "compensation", source = "user.userCompensation")
  JobInformationDto convertToJobInformationDto(JobUser jobUser);

  BasicJobInformationDto convertToBasicJobInformationDto(JobUser jobUser);

  @Mapping(target = "userId", source = "userId")
  @Mapping(target = "employeeFirstName", source = "jobEmployeeDto.firstName")
  @Mapping(target = "employeeLastName", source = "jobEmployeeDto.lastName")
  @Mapping(target = "employeeImageUrl", source = "jobEmployeeDto.imageUrl")
  @Mapping(target = "employeeWorkPhone", source = "jobEmployeeDto.phoneNumber")
  @Mapping(target = "employeeJobTitle", source = "jobEmployeeDto.jobTitle")
  @Mapping(target = "managerId", source = "jobManagerDto.userId")
  @Mapping(target = "managerFirstName", source = "jobManagerDto.firstName")
  @Mapping(target = "managerLastName", source = "jobManagerDto.lastName")
  @Mapping(target = "managerImageUrl", source = "jobManagerDto.imageUrl")
  @Mapping(target = "managerJobTitle", source = "jobManagerDto.jobTitle")
  @Mapping(target = "directReporters", source = "directReporters")
  EmployeeRelatedInformationDto convertToEmployeeRelatedInformationDto(Long userId,
      JobUserDto jobEmployeeDto, JobUserDto jobManagerDto, List<JobUserDto> directReporters);

  @Mapping(target = "job", source = "jobId")
  @Mapping(target = "office", source = "officeId")
  @Mapping(target = "employmentType", source = "employmentTypeId")
  void updateFromJobUpdateDto(@MappingTarget JobUser jobUser, JobUpdateDto jobUpdateDto);

  default Job convertToJob(final Long id) {
    final Job job = new Job();
    job.setId(id);
    return job;
  }

  default Office convertToOffice(final Long id) {
    final Office office = new Office();
    office.setId(id);
    return office;
  }

  default EmploymentType convertToEmploymentType(final Long id) {
    final EmploymentType employmentType = new EmploymentType();
    employmentType.setId(id);
    return employmentType;
  }
}
