package shamu.company.server.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import shamu.company.authorization.Permission;
import shamu.company.authorization.PermissionUtils;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.server.dto.CompanyDto;
import shamu.company.server.dto.CompanyDtoProjection;
import shamu.company.server.dto.CompanyUser;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@Service
public class CompanyUserService {

  private final CompanyService companyService;

  private final UserService userService;

  private final PermissionUtils permissionUtils;

  @Autowired
  public CompanyUserService(
      final CompanyService companyService,
      final UserService userService,
      final PermissionUtils permissionUtils) {
    this.companyService = companyService;
    this.userService = userService;
    this.permissionUtils = permissionUtils;
  }

  public List<User> findAllById(final List<String> ids) {
    return userService.findAllById(ids);
  }

  public User findUserById(final String id) {
    return userService.findById(id);
  }

  public List<User> findAllUsers() {
    return userService.findAllActiveUsers();
  }

  public Page<JobUserListItem> findAllEmployees(
      final EmployeeListSearchCondition employeeListSearchCondition) {
    if (permissionUtils.hasAuthority(Permission.Name.VIEW_DISABLED_USER.name())) {
      employeeListSearchCondition.setIncludeDeactivated(true);
    }
    return userService.findAllEmployeesByName(employeeListSearchCondition);
  }

  public User findUserByUserId(final String userId) {
    return userService.findById(userId);
  }

  public CompanyDtoProjection findCompanyDtoByUserId(final String id) {
    return companyService.findCompanyDtoByUserId(id);
  }

  public List<CompanyDto> findCompaniesByIds(final List<String> ids) {
    final List<Company> companies = companyService.findAllById(ids);
    return companies.stream()
        .map(company -> CompanyDto.builder().id(company.getId()).name(company.getName()).build())
        .collect(Collectors.toList());
  }

  public CompanyUser findSuperUser(final String companyId) {
    final User user = userService.findSuperUser(companyId);
    return new CompanyUser(user);
  }

  public List<User> findAllRegisteredUsers() {
    return userService.findRegisteredUsers();
  }
}
