package shamu.company.server;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shamu.company.common.BaseRestController;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.server.dto.AuthUser;
import shamu.company.server.dto.CompanyDto;
import shamu.company.server.dto.CompanyDtoProjection;
import shamu.company.server.dto.CompanyUser;
import shamu.company.server.dto.DocumentRequestEmailDto;
import shamu.company.server.service.CompanyEmailService;
import shamu.company.server.service.CompanyUserService;

@RestController
@RequestMapping("/server/company")
public class CompanyServerController extends BaseRestController {

  private final CompanyUserService companyUserService;

  private final CompanyEmailService companyEmailService;

  @Autowired
  public CompanyServerController(final CompanyUserService companyUserService,
      final CompanyEmailService companyEmailService) {
    this.companyUserService = companyUserService;
    this.companyEmailService = companyEmailService;
  }

  @GetMapping("/users/current")
  public AuthUser findCurrentUser() {
    return findAuthUser();
  }

  @GetMapping(value = "/users/id")
  public List<CompanyUser> findUsersById(@RequestParam final List<String> ids) {
    return companyUserService.findAllById(ids).parallelStream().map(CompanyUser::new)
        .collect(Collectors.toList());
  }

  @GetMapping(value = "/user/id")
  public CompanyUser findUserById(@RequestParam final String id) {
    return new CompanyUser(companyUserService.findUserById(id));
  }

  @GetMapping(value = "/users")
  public List<CompanyUser> findAllUsers() {
    return companyUserService.findAllUsers(findCurrentUser().getCompanyId()).stream()
        .map(CompanyUser::new).collect(Collectors.toList());
  }

  @PostMapping(value = "/emails")
  public void sendDocumentRequestEmail(
      @RequestBody final DocumentRequestEmailDto documentRequestEmailDto) {
    companyEmailService.sendDocumentRequestEmail(documentRequestEmailDto);
  }

  @GetMapping("/employees")
  public Page<JobUserListItem> findAllEmployeesByName(
      @RequestBody final EmployeeListSearchCondition employeeListSearchCondition) {
    return companyUserService.findAllEmployees(findAuthUser(), employeeListSearchCondition);
  }

  @GetMapping(value = "/user/{id}")
  public AuthUser getUserById(@PathVariable final String id) {
    return new AuthUser(companyUserService.findUserByUserId(id));
  }

  @GetMapping(value = "/companies")
  public List<CompanyDto> findCompaniesByIds(@RequestParam final List<String> ids) {
    return companyUserService.findCompaniesByIds(ids);
  }

  @GetMapping(value = "users/{id}/company")
  public CompanyDtoProjection findCompanyByUserId(@PathVariable final String id) {
    return companyUserService.findCompanyDtoByUserId(id);
  }

  @GetMapping(value = "/users/super")
  public CompanyUser findSuperUser() {
    return companyUserService.findSuperUser(findCompanyId());
  }
}
