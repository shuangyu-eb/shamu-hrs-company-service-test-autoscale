package shamu.company.user.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.context.Context;
import shamu.company.company.entity.Company;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.JobUserDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserStatus.Status;

public interface UserService {

  User findUserById(Long id);

  User findUserByEmail(String email);

  User findUserByEmailAndStatus(String email, Status userStatus);

  User findUserByUserPersonalInformationId(Long userPersonalInformationId);

  User findUserByUserContactInformationId(Long userContactInformationId);

  void sendVerifyEmail(String email);

  void finishUserVerification(String activationToken);

  Boolean existsByEmailWork(String email);

  User findEmployeeInfoByUserId(Long id);

  JobUserDto findEmployeeInfoByEmployeeId(Long id);

  List<JobUserDto> findDirectReportsByManagerId(Long id);

  String getWelcomeEmail(Context context);

  Context getWelcomeEmailContext(String welcomeMessage, String resetPasswordToken);

  Page<JobUserListItem> getAllEmployees(
      EmployeeListSearchCondition employeeListSearchCondition, Company company);

  Page<JobUserListItem> getAllEmployeesByCompany(
      EmployeeListSearchCondition employeeListSearchCondition, Company company, Pageable pageable);

  User getOne(Long userId);

  String getEmployeeNumber(String companyName, Integer employeeNumber);

  void save(User user);

  String getHeadPortrait(Long userId);

  Boolean createPasswordTokenExist(String token);

  void createPassword(UpdatePasswordDto updatePasswordDto);

  Page<JobUserListItem> getMyTeam(EmployeeListSearchCondition employeeListSearchCondition,
      User user);
}
