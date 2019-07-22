package shamu.company.server;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shamu.company.user.entity.User;

@RestController
@RequestMapping("/server/company")
public class CompanyServerController {

  private final CompanyUserService companyUserService;

  @Autowired
  public CompanyServerController(CompanyUserService companyUserService) {
    this.companyUserService = companyUserService;
  }

  @GetMapping("/users/email")
  public AuthUser getUserBy(@RequestParam String email) {
    User user = this.companyUserService.findUserByEmail(email);
    return new AuthUser(user);
  }

  @GetMapping(value = "/users/id")
  public List<CompanyUser> getUsersBy(@RequestParam List<Long> ids) {
    return companyUserService.getUsersBy(ids).parallelStream().map(CompanyUser::new)
        .collect(Collectors.toList());
  }
}
