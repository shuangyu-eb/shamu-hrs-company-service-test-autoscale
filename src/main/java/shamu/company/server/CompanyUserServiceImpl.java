package shamu.company.server;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.entity.Company;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserService;

@Service
public class CompanyUserServiceImpl implements CompanyUserService {

  private final UserRepository userRepository;

  private final CompanyRepository companyRepository;

  private final UserService userService;

  @Autowired
  public CompanyUserServiceImpl(final UserRepository userRepository,
      final CompanyRepository companyRepository,
      final UserService userService) {
    this.userRepository = userRepository;
    this.companyRepository = companyRepository;
    this.userService = userService;
  }

  @Override
  public List<User> getUsersBy(final List<String> ids) {
    return userRepository.findAllById(ids);
  }

  @Override
  public User findUserByEmail(final String email) {
    return userRepository.findByEmailWork(email);
  }

  @Override
  public List<User> getAllUsers(final String companyId) {
    final Company company = companyRepository.findById(companyId)
        .orElseThrow(() -> new ResourceNotFoundException("Company does not exist"));
    return userRepository.findAllByCompanyId(company.getId());
  }

  @Override
  public AuthUser findUserByUserId(final String userId) {
    final User user = userRepository.findByUserId(userId);
    return new AuthUser(user);
  }

  @Override
  public Page<JobUserListItem> getAllEmployees(
          AuthUser user, EmployeeListSearchCondition employeeListSearchCondition) {
    if (user.getRole() == User.Role.ADMIN) {
      employeeListSearchCondition.setIncludeDeactivated(true);
    }
    return userService.getAllEmployeesByName(employeeListSearchCondition, user.getCompanyId());
  }
}
