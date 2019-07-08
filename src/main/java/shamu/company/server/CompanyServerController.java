package shamu.company.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.service.UserService;

@RestController
@RequestMapping("/server/company")
public class CompanyServerController {

  private final UserService userService;

  @Autowired
  public CompanyServerController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users/email/{email}")
  AuthUser getUserBy(@PathVariable String email) {
    User user = this.userService.findUserByEmailAndStatus(email, Status.ACTIVE);
    return new AuthUser(user);
  }

}
