package shamu.company.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.ApplicationConfig;
import shamu.company.email.EmailService;
import shamu.company.s3.AwsUtil;
import shamu.company.server.dto.DocumentRequestEmailDto;
import shamu.company.server.service.CompanyEmailService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyEmailServiceTests {

  @Mock
  private UserService userService;

  @Mock
  private EmailService emailService;

  @Mock
  private ITemplateEngine templateEngine;

  @Mock
  private AwsUtil awsUtil;

  @Mock
  private ApplicationConfig applicationConfig;

  @InjectMocks
  private CompanyEmailService companyEmailService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class FindVariables{
    final DocumentRequestEmailDto documentRequestEmailDto = new DocumentRequestEmailDto();
    final User sender = new User();
    final String s3ImageUrl = "s3 image url";
    final String frontEndAddress = "https://frontEndAddress/";

    @BeforeEach
    void init() {
      UserPersonalInformation personalInformation = new UserPersonalInformation();
      personalInformation.setFirstName("firstName");
      personalInformation.setLastName("lastName");
      sender.setUserPersonalInformation(personalInformation);
    }

    @Test
    void whenSendHasAvatar_thenReturnS3ImageUrl() throws Exception {
      documentRequestEmailDto.setType(DocumentRequestEmailDto.DocumentRequestType.VIEW);
      sender.setImageUrl(Mockito.anyString());
      Mockito.when(awsUtil.getFullFileUrl(sender.getImageUrl())).thenReturn(s3ImageUrl);
      Map<String, Object> variables =  Whitebox.invokeMethod(companyEmailService, "findVariables", documentRequestEmailDto, sender);
      Assertions.assertEquals(s3ImageUrl, variables.get("senderAvatar"));
    }

    @Test
    void whenSendHasNoAvatar_thenReturnDefaultImageUrl() throws Exception {
      documentRequestEmailDto.setType(DocumentRequestEmailDto.DocumentRequestType.VIEW);
      Mockito.when(applicationConfig.getFrontEndAddress()).thenReturn(frontEndAddress);
      Map<String, Object> variables =  Whitebox.invokeMethod(companyEmailService, "findVariables", documentRequestEmailDto, sender);
      Assertions.assertEquals(frontEndAddress.concat("image/person.png"), variables.get("senderAvatar"));
    }

    @Test
    void whenRequestTypeIsNotView_thenShouldIncludeDueDate() throws Exception {
      documentRequestEmailDto.setType(DocumentRequestEmailDto.DocumentRequestType.ACKNOWLEDGE);
      documentRequestEmailDto.setExpiredAt(Timestamp.valueOf(LocalDateTime.now()));
      Map<String, Object> variables =  Whitebox.invokeMethod(companyEmailService, "findVariables", documentRequestEmailDto, sender);
      Assertions.assertNotNull(variables.get("dueDate"));
    }

    @Test
    void whenRequestTypeIsView_thenShouldNotIncludeDueDate() throws Exception {
      documentRequestEmailDto.setType(DocumentRequestEmailDto.DocumentRequestType.VIEW);
      Map<String, Object> variables =  Whitebox.invokeMethod(companyEmailService, "findVariables", documentRequestEmailDto, sender);
      Assertions.assertNull(variables.get("dueDate"));
    }
  }

  @Nested
  class CreateTemplate{
    final DocumentRequestEmailDto documentRequestEmailDto = new DocumentRequestEmailDto();
    final Map<String, Object> variables = new HashMap<>();
    final User sender = new User();

    @BeforeEach
    void init() {
      UserPersonalInformation personalInformation = new UserPersonalInformation();
      personalInformation.setFirstName("firstName");
      personalInformation.setLastName("lastName");
      sender.setUserPersonalInformation(personalInformation);

      final List<String> recipientUserIds = new ArrayList<>();
      final String recipientUserId = Mockito.anyString();
      recipientUserIds.add(recipientUserId);
      documentRequestEmailDto.setRecipientUserIds(recipientUserIds);

      final User recipient = new User();
      UserContactInformation contactInformation = new UserContactInformation();
      contactInformation.setEmailWork("emailWork");
      recipient.setUserPersonalInformation(personalInformation);
      recipient.setUserContactInformation(contactInformation);

      Mockito.when(userService.findById(recipientUserId)).thenReturn(recipient);
      Mockito.when(applicationConfig.getSystemEmailAddress()).thenReturn("emailAddress");
    }

    @Test
    void whenMessageIsNotEmpty_thenVariablesShouldIncludeMessage() throws Exception {
      documentRequestEmailDto.setRecipientUserIds(new ArrayList<>());
      documentRequestEmailDto.setMessage("message");
      Whitebox.invokeMethod(companyEmailService, "createTemplate", documentRequestEmailDto, variables, sender);
      Assertions.assertNotNull(variables.get("message"));
    }

    @Test
    void whenMessageIsEmptyString_thenVariablesShouldNotIncludeMessage() throws Exception {
      documentRequestEmailDto.setRecipientUserIds(new ArrayList<>());
      documentRequestEmailDto.setMessage("");
      Whitebox.invokeMethod(companyEmailService, "createTemplate", documentRequestEmailDto, variables, sender);
      Assertions.assertNull(variables.get("message"));
    }

    @Test
    void whenMessageIsNull_thenVariablesShouldNotIncludeMessage() throws Exception {
      documentRequestEmailDto.setRecipientUserIds(new ArrayList<>());
      documentRequestEmailDto.setMessage(null);
      Whitebox.invokeMethod(companyEmailService, "createTemplate", documentRequestEmailDto, variables, sender);
      Assertions.assertNull(variables.get("message"));
    }

    @Test
    void whenRequestTypeIsSign_thenShouldTemplateIsSignature() throws Exception {
      documentRequestEmailDto.setType(DocumentRequestEmailDto.DocumentRequestType.SIGN);
      Mockito.when(templateEngine.process(Mockito.eq("document_request_signature.html"), Mockito.any())).thenReturn("");
      Whitebox.invokeMethod(companyEmailService, "createTemplate", documentRequestEmailDto, variables, sender);
      Mockito.verify(templateEngine, Mockito.times(1)).process(Mockito.eq("document_request_signature.html"), Mockito.any());
    }

    @Test
    void whenRequestTypeIsAcknowledge_thenShouldTemplateIsAcknowledge() throws Exception {
      documentRequestEmailDto.setType(DocumentRequestEmailDto.DocumentRequestType.ACKNOWLEDGE);
      Mockito.when(templateEngine.process(Mockito.eq("document_request_acknowledge.html"), Mockito.any())).thenReturn("");
      Whitebox.invokeMethod(companyEmailService, "createTemplate", documentRequestEmailDto, variables, sender);
      Mockito.verify(templateEngine, Mockito.times(1)).process(Mockito.eq("document_request_acknowledge.html"), Mockito.any());
    }

    @Test
    void whenRequestTypeIsView_thenShouldTemplateIsView() throws Exception {
      documentRequestEmailDto.setType(DocumentRequestEmailDto.DocumentRequestType.VIEW);
      Mockito.when(templateEngine.process(Mockito.eq("document_request_no_action.html"), Mockito.any())).thenReturn("");
      Whitebox.invokeMethod(companyEmailService, "createTemplate", documentRequestEmailDto, variables, sender);
      Mockito.verify(templateEngine, Mockito.times(1)).process(Mockito.eq("document_request_no_action.html"), Mockito.any());
    }
  }
}
