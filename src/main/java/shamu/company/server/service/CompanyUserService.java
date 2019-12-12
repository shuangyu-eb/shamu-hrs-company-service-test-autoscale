package shamu.company.server.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.server.dto.AuthUser;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@Service
public class CompanyUserService {

  private final CompanyService companyService;

  private final UserService userService;

  @Autowired
  public CompanyUserService(final CompanyService companyService,
      final UserService userService) {
    this.companyService = companyService;
    this.userService = userService;
  }

  public List<User> findAllById(final List<String> ids) {
    return userService.findAllById(ids);
  }

  public List<User> findAllUsers(final String companyId) {
    final Company company = companyService.findById(companyId);
    return userService.findAllByCompanyId(company.getId());
  }

  public Page<JobUserListItem> findAllEmployees(
          AuthUser user, EmployeeListSearchCondition employeeListSearchCondition) {
    if (user.getRole() == User.Role.ADMIN) {
      employeeListSearchCondition.setIncludeDeactivated(true);
    }
    return userService.findAllEmployeesByName(employeeListSearchCondition, user.getCompanyId());
  }
}
