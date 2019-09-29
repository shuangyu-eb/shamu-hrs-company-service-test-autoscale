package shamu.company.user.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.context.Context;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.dto.AccountInfoDto;
import shamu.company.user.dto.CreatePasswordDto;
import shamu.company.user.dto.CurrentUserDto;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserSignUpDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.pojo.ChangePasswordPojo;
import shamu.company.user.pojo.UserRoleUpdatePojo;
import shamu.company.user.pojo.UserStatusUpdatePojo;

public interface UserService {

  User findUserById(Long id);

  User findByUserId(final String userId);

  List<User> findByManagerUser(User managerUser);

  void cacheUser(String token, Long userId);

  User findUserByUserPersonalInformationId(Long userPersonalInformationId);

  User findUserByUserContactInformationId(Long userContactInformationId);

  User findEmployeeInfoByUserId(Long id);

  JobUserDto findEmployeeInfoByEmployeeId(Long id);

  List<User> findDirectReportsByManagerId(Long id);

  String getWelcomeEmail(Context context);

  Context getWelcomeEmailContext(String welcomeMessage, String resetPasswordToken);

  Page<JobUserListItem> getAllEmployees(
      EmployeeListSearchCondition employeeListSearchCondition, Long companyId, Role role);

  Page<JobUserListItem> getAllEmployeesByCompany(
      EmployeeListSearchCondition employeeListSearchCondition,
      Long companyId, Pageable pageable, Role role);

  User getOne(Long userId);

  void save(User user);

  void saveUserWithRole(User user, User.Role role);

  List<JobUserDto> findAllJobUsers(Long companyId);

  List<User> findAllUsersByCompany(Long companyId);

  String getHeadPortrait(Long userId);

  Boolean createPasswordTokenExist(String token);

  void createPassword(CreatePasswordDto createPasswordDto);

  Page<JobUserListItem> getMyTeam(EmployeeListSearchCondition employeeListSearchCondition,
      Long id);

  void sendResetPasswordEmail(String email);

  UserCompensation saveUserCompensation(UserCompensation userCompensation);

  void resetPassword(UpdatePasswordDto updatePasswordDto);

  List<OrgChartDto> getOrgChart(Long userId, Long companyId);

  AccountInfoDto getPreSetAccountInfoByUserId(Long id);

  User updateUserRole(String email, UserRoleUpdatePojo userRoleUpdatePojo, User user);

  User deactivateUser(String email, UserStatusUpdatePojo userStatusUpdatePojo, User user);

  void signUp(UserSignUpDto signUpDto);

  boolean hasUserAccess(User currentUser, Long userId);

  Long getManagerUserIdById(Long userId);

  CurrentUserDto getCurrentUserInfo(String userId);

  CurrentUserDto getMockUserInfo(Long userId);

  Boolean existsByEmailWork(String email);

  void updatePassword(ChangePasswordPojo changePasswordPojo, String email);

  void checkPassword(User user,String password);

  void sendChangeWorkEmail(Long userId, String newEmail);

  void sendVerifyChangeWorkEmail(User user);

  boolean changeWorkEmailTokenExist(String token);
}
