package shamu.company.user.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.context.Context;
import shamu.company.company.entity.Company;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.JobUserDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.dto.PersonalInformationDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserStatus.Status;

public interface UserService {

  User findUserById(Long id);

  User findUserByEmailAndStatus(String email, Status userStatus);

  User findUserByUserPersonalInformationId(Long userPersonalInformationId);

  void sendVerifyEmail(String email);

  void finishUserVerification(String activationToken);

  Boolean existsByEmailWork(String email);

  PersonalInformationDto getPersonalInformation(Long userId);

  User findEmployeeInfoByUserId(Long id);

  JobUserDto findEmployeeInfoByEmployeeId(Long id);

  List<JobUserDto> findDirectReportsByManagerId(Long id);

  String getWelcomeEmail(Context context);

  Context getWelcomeEmailContext(String welcomeMessage);

  PageImpl getJobUserDtoList(EmployeeListSearchCondition employeeListSearchCondition,
      Company company);

  Page<JobUserListItem> findAllEmployees(EmployeeListSearchCondition employeeListSearchCondition,
      Company company, Pageable pageable);

  User getOne(Long userId);
}
