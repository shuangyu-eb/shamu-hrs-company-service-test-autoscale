package shamu.company.user.controller;

import java.io.IOException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.CompanyService;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;
import shamu.company.utils.AwsUtil;
import shamu.company.utils.AwsUtil.Type;

@RestApiController
public class UserRestController extends BaseRestController {

  @Autowired UserService userService;

  @Autowired CompanyService companyService;

  @Autowired AwsUtil awsUtil;

  @PostMapping(value = "user/sign-up/email")
  public HttpEntity sendVerifyEmail(@RequestBody String email) {
    userService.sendVerifyEmail(email);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PostMapping(value = "user/verify")
  public HttpEntity finishUserVerification(@RequestBody String token) {
    userService.finishUserVerification(token);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping(value = "user/check/email/{email}")
  public Boolean checkEmail(@PathVariable String email) {
    return userService.existsByEmailWork(email);
  }

  @GetMapping(value = "user/check/company-name/{companyName}")
  public Boolean checkCompanyName(@PathVariable String companyName) {
    return companyService.existsByName(companyName);
  }

  @GetMapping(value = "user/check/desired-url/{desiredUrl}")
  public Boolean checkDesiredUrl(@PathVariable String desiredUrl) {
    return companyService.existsBySubdomainName(desiredUrl);
  }

  @PostMapping("users/{id}/head-portrait")
  public String handleFileUpload(
      @PathVariable @HashidsFormat Long id, @RequestParam("file") MultipartFile file)
      throws IOException {
    String path = awsUtil.uploadFile(file, Type.IMAGE);

    if (Strings.isBlank(path)) {
      return null;
    }

    User user = userService.findUserById(id);
    String originalPath = user.getImageUrl();
    if (originalPath != null) {
      awsUtil.deleteFile(originalPath);
    }

    user.setImageUrl(path);
    userService.save(user);

    return path;
  }
}
