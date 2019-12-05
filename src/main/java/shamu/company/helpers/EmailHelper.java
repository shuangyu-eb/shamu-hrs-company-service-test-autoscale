package shamu.company.helpers;

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
public class EmailHelper {

  private final SendGrid sendGrid;

  @Autowired
  public EmailHelper(@Value("${sendGrid.apiKey}") final String sendGridKey) {
    sendGrid = new SendGrid(sendGridKey);
  }

  public void send(final shamu.company.email.Email email) {
    final Mail mail = build(email);
    send(mail);
  }

  public void send(final String from, final String to, final String subject, final String content) {
    final Mail mail = build(from, to, subject, content);
    send(mail);
  }

  private void send(final Mail mail) {
    try {
      final Request request = new Request();
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());
      final Response response = sendGrid.api(request);
      final int statusCode = response.getStatusCode();
      final boolean sendResult =
          statusCode == HttpStatus.OK.value() || statusCode == HttpStatus.ACCEPTED.value();
      if (!sendResult) {
        throw new EmailException("Failed to send email!");
      }
    } catch (final IOException e) {
      throw new EmailException(e.getMessage(), e);
    }
  }

  private Mail build(final String from, final String to, final String subject,
      final String content) {
    final Mail mail = new Mail();

    mail.setFrom(new Email(from));
    final Personalization personalization = new Personalization();
    personalization.addTo(new Email(to));

    mail.setSubject(subject);
    mail.addPersonalization(personalization);
    final Content mailBody = new Content("text/html", content);
    mail.addContent(mailBody);
    return mail;
  }

  private Mail build(final shamu.company.email.Email email) {
    final Mail mail = new Mail();
    final String fromName = email.getFromName();
    final String toName = email.getToName();
    final Email from = new Email(email.getFrom());
    final Email to = new Email(email.getTo());
    if (Strings.isNotBlank(fromName)) {
      from.setName(fromName);
    }
    if (Strings.isNotBlank(toName)) {
      to.setName(toName);
    }
    mail.setFrom(from);
    final Personalization personalization = new Personalization();
    personalization.addTo(to);

    mail.setSubject(email.getSubject());
    mail.addPersonalization(personalization);
    final Content mailBody = new Content("text/html", email.getContent());
    mail.addContent(mailBody);

    return mail;
  }
}
