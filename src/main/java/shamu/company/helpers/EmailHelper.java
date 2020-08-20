package shamu.company.helpers;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Personalization;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import shamu.company.helpers.exception.EmailSendFailedException;

import java.io.IOException;
import java.util.List;

@Component
public class EmailHelper {

  private static final String EMAIL_UNIQUE_ID = "messageId";
  private static final String EMAIL_CONTENT = "email_content";
  private static final int MAX_NUMBER_OF_MULTIPLE_RECIPIENTS = 500;
  private final SendGrid sendGrid;

  @Autowired
  public EmailHelper(@Value("${sendGrid.apiKey}") final String sendGridKey) {
    sendGrid = new SendGrid(sendGridKey);
  }

  public void send(final shamu.company.email.entity.Email email) {
    final Mail mail = build(email);
    send(mail);
  }

  public void send(final List<shamu.company.email.entity.Email> toList) {
    if (toList.isEmpty()) {
      return;
    }

    final shamu.company.email.entity.Email email = toList.get(0);
    final Email from = new Email();
    from.setName(email.getFromName());
    from.setEmail(email.getFrom());
    for (int i = 0; i < toList.size(); i += MAX_NUMBER_OF_MULTIPLE_RECIPIENTS) {
      final int rearIndex = Math.min(i + MAX_NUMBER_OF_MULTIPLE_RECIPIENTS, toList.size());
      final List<shamu.company.email.entity.Email> subList = toList.subList(i, rearIndex);
      final Mail mail = build(subList, from, email.getSubject(), email.getContent());
      send(mail);
    }
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
        throw new EmailSendFailedException("Failed to send email.");
      }
    } catch (final IOException e) {
      throw new EmailSendFailedException(e.getMessage(), e);
    }
  }

  private Mail build(final shamu.company.email.entity.Email email) {
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
    personalization.addCustomArg(EMAIL_UNIQUE_ID, email.getMessageId());
    personalization.addTo(to);

    mail.setSubject(email.getSubject());
    mail.addPersonalization(personalization);
    addContent(mail, email.getContent());

    return mail;
  }

  private Mail build(
      final List<shamu.company.email.entity.Email> toList,
      final Email from,
      final String subject,
      final String content) {
    final Mail mail = new Mail();
    mail.setFrom(from);
    mail.setSubject(subject);

    toList.forEach(
        toEmail -> {
          final Personalization personalization = new Personalization();
          final Email to = new Email();
          to.setName(toEmail.getToName());
          to.setEmail(toEmail.getTo());
          personalization.addTo(to);
          personalization.addSubstitution(EMAIL_CONTENT, content);
          mail.addPersonalization(personalization);
        });

    addContent(mail, EMAIL_CONTENT);

    return mail;
  }

  private Mail addContent(final Mail mail, final String content) {
    mail.addContent(new Content("text/html", content));
    return mail;
  }
}
