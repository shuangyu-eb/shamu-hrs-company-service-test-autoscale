package shamu.company.config;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;
import shamu.company.utils.UuidUtil;

@Component
public class UserInformationGenerator implements CommandLineRunner {
  private final UserService userService;
  private final UserRoleService userRoleService;
  private final CompanyService companyService;
  private String userId;
  private String companyId;

  @Autowired
  public UserInformationGenerator(
      final UserService userService,
      final UserRoleService userRoleService,
      final CompanyService companyService) {
    this.userService = userService;
    this.userRoleService = userRoleService;
    this.companyService = companyService;
  }

  @Override
  public void run(final String... args) {
    saveCompany();
    saveUser();
    populateAuthentication();
  }

  private void saveCompany() {
    final Company company = companyService.save(new Company());
    companyId = company.getId();
  }

  private void saveUser() {
    userId = UuidUtil.getUuidString();

    final User user = new User(userId);
    final UserContactInformation userContactInformation = new UserContactInformation();
    userContactInformation.setEmailWork("example@example.com");
    user.setUserContactInformation(userContactInformation);

    final Company company = new Company(companyId);

    final UserRole userRole = userRoleService.getAdmin();
    user.setUserRole(userRole);
    userService.save(user);
  }

  private void populateAuthentication() {
    final AuthUser authUser = new AuthUser();
    authUser.setId(userId);
    authUser.setCompanyId(companyId);
    authUser.setEmail("example@example.com");
    authUser.setUserId(userId);
    authUser.setPermissions(Collections.emptyList());
    final DefaultJwtAuthenticationToken defaultJwtAuthenticationToken =
        new DefaultJwtAuthenticationToken(
            JwtUtil.getJwt(), authUser.getId(), Collections.emptyList(), authUser);
    SecurityContextHolder.getContext().setAuthentication(defaultJwtAuthenticationToken);
  }
}
