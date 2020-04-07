package shamu.company.helpers;

import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.http.HttpStatus;
import shamu.company.common.exception.EmailException;
import shamu.company.email.entity.Email;
import shamu.company.email.event.EmailStatus;
import shamu.company.user.entity.User;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;

class EmailHelperTests {

  private final Email email = new Email();
  private final EmailHelper emailHelper = new EmailHelper("1");;
  @Mock private SendGrid sendGrid;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    email.setContent("1");
    email.setSendDate(Timestamp.from(Instant.now()));
    email.setStatus(EmailStatus.PROCESSED);
    email.setTo("to@indeed.com");
    email.setFrom("from@indeed.com");
    email.setUser(new User());
    Whitebox.setInternalState(emailHelper, "sendGrid", sendGrid);
  }

  @Nested
  class SendEmail {

    @Test
    void whenEntityStatusFalse_thenShouldThrow() throws IOException {
      Response response = Mockito.mock(Response.class);
      Mockito.when(sendGrid.api(Mockito.any())).thenReturn(response);
      Assertions.assertThrows(EmailException.class, () -> emailHelper.send(email));
    }

    @Test
    void whenEntityStatusTrue_thenShouldSuccess() throws IOException {
      Response response = new Response();
      response.setStatusCode(HttpStatus.OK.value());
      Mockito.when(sendGrid.api(Mockito.any())).thenReturn(response);
      Assertions.assertDoesNotThrow(() -> emailHelper.send(email));
    }

    @Test
    void whenParamStatusFalse_thenShouldThrow() throws IOException {
      Response response = Mockito.mock(Response.class);
      Mockito.when(sendGrid.api(Mockito.any())).thenReturn(response);
      Assertions.assertThrows(EmailException.class, () -> emailHelper.send(
          email.getFrom(),email.getTo(),email.getSubject(),email.getContent()));
    }

    @Test
    void whenParamStatusTrue_thenShouldSuccess() throws IOException {
      Response response = new Response();
      response.setStatusCode(HttpStatus.OK.value());
      Mockito.when(sendGrid.api(Mockito.any())).thenReturn(response);
      Assertions.assertDoesNotThrow(() -> emailHelper.send(
          email.getFrom(),email.getTo(),email.getSubject(),email.getContent()));
    }
  }

}


