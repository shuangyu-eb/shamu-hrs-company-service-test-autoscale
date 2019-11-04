package shamu.company.job.entity.mapper;

import java.sql.Timestamp;
import java.util.List;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.common.mapper.Config;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.EmployeeRelatedInformationDto;
import shamu.company.employee.dto.JobInformationDto;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserDto;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.mapper.UserCompensationMapper;

@Mapper(
    config = Config.class,
    uses = {SelectFieldInformationDto.class, OfficeMapper.class, UserCompensationMapper.class}
)
public interface JobUserMapper {

  @Mapping(target = "jobUserId", source = "user.id")
  @Mapping(target = "department", source = "job.department")
  @Mapping(target = "manager", source = "user.managerUser")
  @Mapping(target = "manager.name", source = "user.managerUser.userPersonalInformation.name")
  @Mapping(target = "manager.id", source = "user.managerUser.id")
  @Mapping(target = "job.name", source = "job.title")
  @Mapping(target = "userRole", source = "user.userRole")
  BasicJobInformationDto convertToBasicJobInformationDto(JobUser jobUser);

  @InheritConfiguration
  @Mapping(target = "compensation", source = "user.userCompensation")
  JobInformationDto convertToJobInformationDto(JobUser jobUser);

  @Mapping(target = "userId", source = "userId")
  @Mapping(target = "userStatus", source = "userStatus")
  @Mapping(target = "emailSendDate", source = "emailSendDate")
  @Mapping(target = "employeeWorkEmail", source = "email")
  @Mapping(target = "employeeFirstName", source = "jobEmployeeDto.firstName")
  @Mapping(target = "employeeLastName", source = "jobEmployeeDto.lastName")
  @Mapping(target = "employeeImageUrl", source = "jobEmployeeDto.imageUrl")
  @Mapping(target = "employeeWorkPhone", source = "jobEmployeeDto.phoneNumber")
  @Mapping(target = "employeeJobTitle", source = "jobEmployeeDto.jobTitle")
  @Mapping(target = "managerId", source = "jobManagerDto.id")
  @Mapping(target = "managerFirstName", source = "jobManagerDto.firstName")
  @Mapping(target = "managerLastName", source = "jobManagerDto.lastName")
  @Mapping(target = "managerImageUrl", source = "jobManagerDto.imageUrl")
  @Mapping(target = "managerJobTitle", source = "jobManagerDto.jobTitle")
  @Mapping(target = "directReporters", source = "directReporters")
  @Mapping(target = "roleName", source = "roleName")
  EmployeeRelatedInformationDto convertToEmployeeRelatedInformationDto(Long userId, String email,
      String userStatus, Timestamp emailSendDate, JobUserDto jobEmployeeDto,
      JobUserDto jobManagerDto, List<JobUserDto> directReporters, String roleName);

  @Mapping(target = "jobTitle", source = "jobUser.job.title")
  @Mapping(target = "firstName", source = "user.userPersonalInformation.firstName")
  @Mapping(target = "lastName", source = "user.userPersonalInformation.lastName")
  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "imageUrl", source = "user.imageUrl")
  TimeOffPolicyRelatedUserDto convertToTimeOffPolicyRelatedUserDto(User user, JobUser jobUser);

  @Mapping(target = "jobTitle", source = "jobUser.job.title")
  @Mapping(target = "firstName", source = "policyUser.user.userPersonalInformation.firstName")
  @Mapping(target = "lastName", source = "policyUser.user.userPersonalInformation.lastName")
  @Mapping(target = "id", source = "policyUser.user.id")
  @Mapping(target = "imageUrl", source = "policyUser.user.imageUrl")
  @Mapping(target = "balance", source = "policyUser.initialBalance")
  TimeOffPolicyRelatedUserDto convertToTimeOffPolicyRelatedUserDto(TimeOffPolicyUser policyUser,
      JobUser jobUser);

  @Mapping(target = "job", source = "jobId")
  @Mapping(target = "office", source = "officeId")
  @Mapping(target = "employmentType", source = "employmentTypeId")
  void updateFromJobUpdateDto(@MappingTarget JobUser jobUser, JobUpdateDto jobUpdateDto);

  default Job convertToJob(final Long jobId) {
    if (jobId != null) {
      final Job job = new Job();
      job.setId(jobId);
      return job;
    }
    return null;
  }

  default Office convertFromOfficeId(final Long officeId) {
    if (officeId != null) {
      final Office office = new Office();
      office.setId(officeId);
      return office;
    }
    return null;
  }

  default EmploymentType convertFromEmploymentTypeId(final Long employmentTypeId) {
    if (employmentTypeId != null) {
      final EmploymentType employmentType = new EmploymentType();
      employmentType.setId(employmentTypeId);
      return employmentType;
    }
    return null;
  }

  default Role convertToRole(final UserRole userRole) {
    return Role.valueOf(userRole.getName());
  }
}
