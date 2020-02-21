package shamu.company.email.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.email.event.EmailEvent;
import shamu.company.email.service.EmailService;

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
