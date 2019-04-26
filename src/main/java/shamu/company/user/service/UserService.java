package shamu.company.user.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import shamu.company.company.entity.Company;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.JobUserDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.dto.PersonalInformationDto;
import shamu.company.user.entity.User;

public interface UserService {

  User findUserById(Long id);

  User findUserByEmail(String email);

  User findUserByUserPersonalInformationId(Long userPersonalInformationId);

  void sendVerifyEmail(String email);

  void finishUserVerification(String activationToken);

  Boolean existsByEmailWork(String email);

  PersonalInformationDto getPersonalInformation(Long userId);

  User findEmployeeInfoByUserId(Long id);

  JobUserDto findEmployeeInfoByEmployeeId(Long id);

  List<JobUserDto> findDirectReportsByManagerId(Long id);

  String getWelcomeEmail(String personalInformation);

  PageImpl getJobUserDtoList(EmployeeListSearchCondition employeeListSearchCondition,
      Company company);

  Page<JobUserListItem> findAllEmployees(EmployeeListSearchCondition employeeListSearchCondition,
      Company company, Pageable pageable);

  User getOne(Long userId);
}
