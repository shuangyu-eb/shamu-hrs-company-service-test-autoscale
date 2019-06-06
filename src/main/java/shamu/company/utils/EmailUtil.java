package shamu.company.utils;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Personalization;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import java.io.IOException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import shamu.company.common.exception.EmailException;

@Component
public class EmailUtil {

  private static SendGrid sendGrid;

  @Autowired
  public EmailUtil(@Value("${sendGrid.apiKey}") String sendGridKey) {
    sendGrid = new SendGrid(sendGridKey);
  }

  public void send(shamu.company.email.Email email) {
    Mail mail = build(email);
    send(mail);
  }

  public void send(String from, String to, String subject, String content) {
    Mail mail = build(from, to, subject, content);
    send(mail);
  }

  private void send(Mail mail) {
    try {
      Request request = new Request();
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());
      Response response = sendGrid.api(request);
      int statusCode = response.getStatusCode();
      boolean sendResult =
          statusCode == HttpStatus.OK.value() || statusCode == HttpStatus.ACCEPTED.value();
      if (!sendResult) {
        throw new EmailException("Failed to send email!");
      }
    } catch (IOException e) {
      throw new EmailException(e.getMessage(), e);
    }
  }

  private Mail build(String from, String to, String subject, String content) {
    Mail mail = new Mail();

    mail.setFrom(new Email(from));
    Personalization personalization = new Personalization();
    personalization.addTo(new Email(to));

    mail.setSubject(subject);
    mail.addPersonalization(personalization);
    Content mailBody = new Content("text/html", content);
    mail.addContent(mailBody);
    return mail;
  }

  private Mail build(shamu.company.email.Email email) {
    Mail mail = new Mail();
    String fromName = email.getFromName();
    String toName = email.getToName();
    Email from = new Email(email.getFrom());
    Email to = new Email(email.getTo());
    if (Strings.isNotBlank(fromName)) {
      from.setName(fromName);
    }
    if (Strings.isNotBlank(toName)) {
      to.setName(toName);
    }
    mail.setFrom(from);
    Personalization personalization = new Personalization();
    personalization.addTo(to);

    mail.setSubject(email.getSubject());
    mail.addPersonalization(personalization);
    Content mailBody = new Content("text/html", email.getContent());
    mail.addContent(mailBody);

    return mail;
  }
}
