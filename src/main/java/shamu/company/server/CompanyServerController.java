package shamu.company.server;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shamu.company.common.BaseRestController;

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
  public AuthUser getCurrentUser() {
    return new AuthUser(this.getUser());
  }

  @GetMapping(value = "/users/id")
  public List<CompanyUser> getUsersBy(@RequestParam final List<Long> ids) {
    return companyUserService.getUsersBy(ids).parallelStream().map(CompanyUser::new)
        .collect(Collectors.toList());
  }

  @GetMapping(value = "/users")
  public List<CompanyUser> getAllUsers() {
    return companyUserService.getAllUsers(getCurrentUser().getCompanyId()).stream()
        .map(CompanyUser::new).collect(Collectors.toList());
  }

  @PostMapping(value = "/emails")
  public void sendDocumentRequestEmail(
      @RequestBody final DocumentRequestEmailDto documentRequestEmailDto) {
    companyEmailService.sendDocumentRequestEmail(documentRequestEmailDto);
  }
}
