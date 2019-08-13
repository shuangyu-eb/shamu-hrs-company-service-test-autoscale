package shamu.company.company.entity.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.mapper.Config;
import shamu.company.common.mapper.SelectFieldInformationDtoUtils;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.EmployeeRelatedInformationDto;
import shamu.company.employee.dto.JobInformationDto;
import shamu.company.job.dto.JobUserDto;
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
}
