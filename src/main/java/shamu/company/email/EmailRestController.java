package shamu.company.email;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.config.annotations.RestApiController;

@RestApiController
public class EmailRestController {

  private final EmailService emailService;

  @Autowired
  public EmailRestController(final EmailService emailService) {
    this.emailService = emailService;
  }

  @PostMapping("emails/status")
  public HttpStatus updateEmailStatus(@RequestBody final List<EmailEvent> emailEvent) {
    emailService.updateEmailStatus(emailEvent);
    return HttpStatus.OK;
  }
}
