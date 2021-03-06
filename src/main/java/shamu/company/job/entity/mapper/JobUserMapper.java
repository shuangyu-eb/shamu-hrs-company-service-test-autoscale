package shamu.company.job.entity.mapper;

import java.sql.Timestamp;
import java.util.List;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.util.StringUtils;
import shamu.company.attendance.dto.TimeAndAttendanceRelatedUserDto;
import shamu.company.benefit.dto.BenefitPlanUserDto;
import shamu.company.common.mapper.Config;
import shamu.company.company.entity.Department;
import shamu.company.company.dto.OfficeAddressDto;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.EmployeeDetailDto;
import shamu.company.employee.dto.JobInformationDto;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserDto;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.user.entity.EmployeeType;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.mapper.UserCompensationMapper;

@Mapper(
    config = Config.class,
    uses = {SelectFieldInformationDto.class, OfficeMapper.class, UserCompensationMapper.class})
public interface JobUserMapper {

  @Mapping(target = "jobUserId", source = "user.id")
  @Mapping(target = "department", source = "department")
  @Mapping(target = "manager", source = "user.managerUser")
  @Mapping(target = "manager.name", source = "user.managerUser.userPersonalInformation.name")
  @Mapping(target = "manager.id", source = "user.managerUser.id")
  @Mapping(target = "job.name", source = "job.title")
  @Mapping(target = "userRole", source = "user.userRole")
  BasicJobInformationDto convertToBasicJobInformationDto(JobUser jobUser);

  @InheritConfiguration
  @Mapping(target = "compensation", source = "userCompensation")
  @Mapping(target = "employeeType", source = "employeeType")
  JobInformationDto convertToJobInformationDto(JobUser jobUser);

  @Mapping(target = "street1", source = "jobUser.office.officeAddress.street1")
  @Mapping(target = "street2", source = "jobUser.office.officeAddress.street2")
  @Mapping(target = "city", source = "jobUser.office.officeAddress.city")
  @Mapping(target = "stateId", source = "jobUser.office.officeAddress.stateProvince.id")
  @Mapping(target = "stateName", source = "jobUser.office.officeAddress.stateProvince.name")
  @Mapping(target = "postalCode", source = "jobUser.office.officeAddress.postalCode")
  @Mapping(target = "countryId", source = "jobUser.office.officeAddress.stateProvince.country.id")
  @Mapping(target = "countryName", source = "jobUser.office.officeAddress.stateProvince.country.name")
  OfficeAddressDto convertToOfficeAddressDto(JobUser jobUser);

  @Mapping(target = "userStatus", source = "userStatus")
  @Mapping(target = "emailSendDate", source = "emailSendDate")
  @Mapping(target = "workEmail", source = "email")
  @Mapping(target = "firstName", source = "jobEmployeeDto.firstName")
  @Mapping(target = "lastName", source = "jobEmployeeDto.lastName")
  @Mapping(target = "preferredName", source = "jobEmployeeDto.preferredName")
  @Mapping(target = "imageUrl", source = "jobEmployeeDto.imageUrl")
  @Mapping(target = "workPhone", source = "jobEmployeeDto.phoneNumber")
  @Mapping(target = "jobTitle", source = "jobEmployeeDto.jobTitle")
  @Mapping(target = "manager.userId", source = "jobManagerDto.id")
  @Mapping(target = "manager.firstName", source = "jobManagerDto.firstName")
  @Mapping(target = "manager.lastName", source = "jobManagerDto.lastName")
  @Mapping(target = "manager.preferredName", source = "jobManagerDto.preferredName")
  @Mapping(target = "manager.imageUrl", source = "jobManagerDto.imageUrl")
  @Mapping(target = "manager.jobTitle", source = "jobManagerDto.jobTitle")
  @Mapping(target = "directReporters", source = "directReporters")
  @Mapping(target = "roleName", source = "roleName")
  @Mapping(target = "invitationValid", source = "isInvitationValid")
  EmployeeDetailDto convertToEmployeeRelatedInformationDto(
      String email,
      String userStatus,
      Timestamp emailSendDate,
      JobUserDto jobEmployeeDto,
      JobUserDto jobManagerDto,
      List<JobUserDto> directReporters,
      String roleName,
      boolean isInvitationValid);

  @Mapping(target = "jobTitle", source = "jobUser.job.title")
  @Mapping(target = "employmentType", source = "jobUser.employmentType.name")
  @Mapping(target = "department", source = "jobUser.department.name")
  @Mapping(target = "firstName", source = "user.userPersonalInformation.firstName")
  @Mapping(target = "preferredName", source = "user.userPersonalInformation.preferredName")
  @Mapping(target = "lastName", source = "user.userPersonalInformation.lastName")
  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "imageUrl", source = "user.imageUrl")
  TimeOffPolicyRelatedUserDto convertToTimeOffPolicyRelatedUserDto(
      User user, JobUser jobUser, String name, boolean unSelectable);

  @Mapping(target = "jobTitle", source = "jobUser.job.title")
  @Mapping(target = "employmentType", source = "jobUser.employmentType.name")
  @Mapping(target = "department", source = "jobUser.department.name")
  @Mapping(target = "firstName", source = "policyUser.user.userPersonalInformation.firstName")
  @Mapping(
      target = "preferredName",
      source = "policyUser.user.userPersonalInformation.preferredName")
  @Mapping(target = "lastName", source = "policyUser.user.userPersonalInformation.lastName")
  @Mapping(target = "id", source = "policyUser.user.id")
  @Mapping(target = "imageUrl", source = "policyUser.user.imageUrl")
  @Mapping(target = "balance", source = "policyUser.initialBalance")
  TimeOffPolicyRelatedUserDto convertToTimeOffPolicyRelatedUserDto(
      TimeOffPolicyUser policyUser, JobUser jobUser,String name, boolean unSelectable);

  @Mapping(target = "jobTitle", source = "jobUser.job.title")
  @Mapping(target = "employmentType", source = "jobUser.employmentType.name")
  @Mapping(target = "department", source = "jobUser.department.name")
  @Mapping(target = "startDate", source = "jobUser.startDate")
  @Mapping(target = "compensation", source = "jobUser.userCompensation")
  @Mapping(target = "compensation.wage", expression = "java(userCompensationMapper.updateCompensationDollar(userCompensation))")
  @Mapping(target = "firstName", source = "user.userPersonalInformation.firstName")
  @Mapping(
          target = "preferredName",
          source = "user.userPersonalInformation.preferredName")
  @Mapping(target = "lastName", source = "user.userPersonalInformation.lastName")
  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "imageUrl", source = "user.imageUrl")
  @Mapping(
      target = "compensation.overtimePolicy",
      source = "jobUser.userCompensation.overtimePolicy")
  @Mapping(target = "name", source = "name")
  TimeAndAttendanceRelatedUserDto convertToTimeAndAttendanceRelatedUserDto(
      User user, JobUser jobUser, String name);

  @Mapping(target = "department", source = "departmentId")
  @Mapping(target = "job", source = "jobId")
  @Mapping(target = "office", source = "officeId")
  @Mapping(target = "employmentType", source = "employmentTypeId")
  @Mapping(target = "employeeType", source = "employeeTypeId")
  @Mapping(
      target = "startDate",
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
  void updateFromJobUpdateDto(@MappingTarget JobUser jobUser, JobUpdateDto jobUpdateDto);

  BenefitPlanUserDto covertToBenefitPlanUserDto(JobUserDto jobUserDto);

  default Job convertToJob(final String jobId) {
    if (!StringUtils.isEmpty(jobId)) {
      final Job job = new Job();
      job.setId(jobId);
      return job;
    }
    return null;
  }

  default Department convertToDepartment(final String departmentId) {
    if(!StringUtils.isEmpty(departmentId)) {
      final Department department = new Department();
      department.setId(departmentId);
      return department;
    }
    return null;
  }

  default Office convertFromOfficeId(final String officeId) {
    if (!StringUtils.isEmpty(officeId)) {
      final Office office = new Office();
      office.setId(officeId);
      return office;
    }
    return null;
  }

  default EmploymentType convertFromEmploymentTypeId(final String employmentTypeId) {
    if (!StringUtils.isEmpty(employmentTypeId)) {
      final EmploymentType employmentType = new EmploymentType();
      employmentType.setId(employmentTypeId);
      return employmentType;
    }
    return null;
  }

  default EmployeeType convertFromEmployeeTypeId(final String employeeTypeId) {
    if (!StringUtils.isEmpty(employeeTypeId)) {
      final EmployeeType employeeType = new EmployeeType();
      employeeType.setId(employeeTypeId);
      return employeeType;
    }
    return null;
  }

  default Role convertToRole(final UserRole userRole) {
    return Role.valueOf(userRole.getName());
  }
}
