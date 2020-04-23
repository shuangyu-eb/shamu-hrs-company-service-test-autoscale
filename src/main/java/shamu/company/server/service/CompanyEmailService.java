package shamu.company.server.service;

import static shamu.company.server.dto.DocumentRequestEmailDto.DocumentRequestType.ACKNOWLEDGE;
import static shamu.company.server.dto.DocumentRequestEmailDto.DocumentRequestType.SIGN;
import static shamu.company.server.dto.DocumentRequestEmailDto.DocumentRequestType.VIEW;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.ApplicationConfig;
import shamu.company.email.entity.Email;
import shamu.company.email.service.EmailService;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.server.dto.DocumentRequestEmailDto;
import shamu.company.server.dto.DocumentRequestEmailDto.DocumentRequestType;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;
import shamu.company.utils.AvatarUtil;
import shamu.company.utils.DateUtil;

@Service
public class CompanyEmailService {

  private final UserService userService;

  private final EmailService emailService;

  private final ITemplateEngine templateEngine;

  private final AwsHelper awsHelper;

  private final ApplicationConfig applicationConfig;

  @Autowired
  public CompanyEmailService(
      final UserService userService,
      final EmailService emailService,
      final ITemplateEngine templateEngine,
      final AwsHelper awsHelper,
      final ApplicationConfig applicationConfig) {
    this.userService = userService;
    this.emailService = emailService;
    this.templateEngine = templateEngine;
    this.awsHelper = awsHelper;
    this.applicationConfig = applicationConfig;
  }

  public void sendDocumentRequestEmail(final DocumentRequestEmailDto documentRequestEmailDto) {

    final String senderId = documentRequestEmailDto.getSenderId();
    final User sender = userService.findById(senderId);

    final Map<String, Object> variables = findVariables(documentRequestEmailDto, sender);
    createTemplate(documentRequestEmailDto, variables, sender);
  }

  private Map<String, Object> findVariables(
      final DocumentRequestEmailDto documentRequestEmailDto, final User sender) {
    final String documentUrl = documentRequestEmailDto.getDocumentEmailUrl();
    final String documentTitle = documentRequestEmailDto.getDocumentTitle();
    final Map<String, Object> variables = new HashMap<>();

    variables.put("frontEndAddress", applicationConfig.getFrontEndAddress());
    variables.put("helpUrl", applicationConfig.getHelpUrl());

    final UserPersonalInformation senderPersonalInformation = sender.getUserPersonalInformation();
    final String senderName = senderPersonalInformation.getName();
    final String senderAvatar =
        sender.getImageUrl() != null ? awsHelper.findFullFileUrl(sender.getImageUrl()) : null;

    variables.put("senderName", senderName);
    if (StringUtils.isNotEmpty(senderAvatar)) {
      variables.put("senderAvatar", senderAvatar);
    }

    final String backgroundColor =
        AvatarUtil.getAvatarBackground(senderPersonalInformation.getFirstName());
    variables.put("backgroundColor", backgroundColor);
    final String avatarText =
        AvatarUtil.getAvatarShortName(
            senderPersonalInformation.getFirstName(), senderPersonalInformation.getLastName());
    variables.put("avatarText", avatarText);

    variables.put("documentUrl", documentUrl);
    variables.put("documentTitle", documentTitle);

    if (!VIEW.equals(documentRequestEmailDto.getType())
        && null != documentRequestEmailDto.getExpiredAt()) {
      // utc to cst
      final ZonedDateTime zonedDateTime =
          ZonedDateTime.of(
              documentRequestEmailDto.getExpiredAt().toLocalDateTime(), ZoneId.of("UTC"));
      variables.put(
          "dueDate",
          DateUtil.formatDateTo(
              zonedDateTime.withZoneSameInstant(ZoneId.of("America/Managua")).toLocalDateTime(),
              "MMM dd"));
    }

    return variables;
  }

  private void createTemplate(
      final DocumentRequestEmailDto documentRequestEmailDto,
      final Map<String, Object> variables,
      final User sender) {
    final String message = documentRequestEmailDto.getMessage();
    if (StringUtils.isNotEmpty(message)) {
      variables.put("message", "\"" + message + "\"");
    }

    final DocumentRequestType type = documentRequestEmailDto.getType();
    final List<String> recipientUserIds = documentRequestEmailDto.getRecipientUserIds();
    recipientUserIds.forEach(
        recipientUserId -> {
          final User recipient = userService.findById(recipientUserId);
          String template = "";
          String subject = "";
          if (SIGN.equals(type)) {
            template = "document_request_signature.html";
            subject = "Signature Request";
          } else if (ACKNOWLEDGE.equals(type)) {
            template = "document_request_acknowledge.html";
            subject = "Acknowledgement Request";
          } else if (VIEW.equals(type)) {
            final UserPersonalInformation personalInformation = sender.getUserPersonalInformation();
            final String firstName = personalInformation.getFirstName();
            final String preferredName = personalInformation.getPreferredName();
            template = "document_request_no_action.html";
            subject =
                (StringUtils.isEmpty(preferredName) ? firstName : preferredName)
                    + " Sent You a Document";
          }
          final Email email =
              new Email(
                  applicationConfig.getSystemEmailAddress(),
                  sender.getUserPersonalInformation().getName(),
                  recipient.getUserContactInformation().getEmailWork(),
                  recipient.getUserPersonalInformation().getName(),
                  subject);
          sendEmail(variables, template, email);
        });
  }

  private void sendEmail(
      final Map<String, Object> variables, final String template, final Email email) {
    final String emailContent =
        templateEngine.process(template, new Context(Locale.ENGLISH, variables));
    email.setSendDate(new Timestamp(new Date().getTime()));
    email.setContent(emailContent);

    emailService.saveAndScheduleEmail(email);
  }
}
