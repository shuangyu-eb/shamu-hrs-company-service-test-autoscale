package shamu.company.user.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.context.Context;
import shamu.company.company.entity.Company;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.dto.AccountInfoDto;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserLoginDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.pojo.UserRoleUpdatePojo;
import shamu.company.user.pojo.UserStatusUpdatePojo;

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

  List<User> findDirectReportsByManagerId(Long id);

  String getWelcomeEmail(Context context);

  Context getWelcomeEmailContext(String welcomeMessage, String resetPasswordToken);

  Page<JobUserListItem> getAllEmployees(
      EmployeeListSearchCondition employeeListSearchCondition, Company company, Boolean isAdmin);

  Page<JobUserListItem> getAllEmployeesByCompany(
          EmployeeListSearchCondition employeeListSearchCondition,
          Company company, Pageable pageable, Boolean isAdmin);

  User getOne(Long userId);

  String getEmployeeNumber(String companyName, Integer employeeNumber);

  void save(User user);

  void saveUserWithRole(User user, User.Role role);

  List<JobUserDto> findAllJobUsers(Company company);

  List<User> findAllUsersByCompany(Company company);

  String getHeadPortrait(Long userId);

  Boolean createPasswordTokenExist(String token);

  void createPassword(UpdatePasswordDto updatePasswordDto);

  Page<JobUserListItem> getMyTeam(EmployeeListSearchCondition employeeListSearchCondition,
      User user);

  void sendResetPasswordEmail(String email);

  UserCompensation saveUserCompensation(UserCompensation userCompensation);

  void unlock(UserLoginDto userLoginDto);

  boolean resetPassword(UpdatePasswordDto updatePasswordDto);

  OrgChartDto getOrgChart(Long userId, Company currentCompany);

  AccountInfoDto getPreSetAccountInfoByUserId(Long id);

  User updateUserRole(User currentUser, UserRoleUpdatePojo userRoleUpdatePojo, User user);

  User updateUserStatus(User currentUser, UserStatusUpdatePojo userStatusUpdatePojo, User user);
}
