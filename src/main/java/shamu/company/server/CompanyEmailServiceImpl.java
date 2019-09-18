package shamu.company.server;

import static shamu.company.server.DocumentRequestEmailDto.DocumentRequestType.ACKNOWLEDGE;
import static shamu.company.server.DocumentRequestEmailDto.DocumentRequestType.SIGN;
import static shamu.company.server.DocumentRequestEmailDto.DocumentRequestType.VIEW;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.ApplicationConfig;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.email.Email;
import shamu.company.email.EmailService;
import shamu.company.server.DocumentRequestEmailDto.DocumentRequestType;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRepository;
import shamu.company.utils.AwsUtil;
import shamu.company.utils.DateUtil;
import shamu.company.utils.UserNameUtil;

@Service
public class CompanyEmailServiceImpl implements CompanyEmailService {

  private final UserRepository userRepository;

  private final EmailService emailService;

  private final ITemplateEngine templateEngine;

  private final AwsUtil awsUtil;

  private final ApplicationConfig applicationConfig;

  @Autowired
  public CompanyEmailServiceImpl(final UserRepository userRepository,
      final EmailService emailService,
      final ITemplateEngine templateEngine,
      final AwsUtil awsUtil,
      final ApplicationConfig applicationConfig) {
    this.userRepository = userRepository;
    this.emailService = emailService;
    this.templateEngine = templateEngine;
    this.awsUtil = awsUtil;
    this.applicationConfig = applicationConfig;
  }

  @Override
  public void sendDocumentRequestEmail(final DocumentRequestEmailDto documentRequestEmailDto) {

    final Long senderId = documentRequestEmailDto.getSenderId();
    final User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    final Map<String, Object> variables = getVariables(documentRequestEmailDto, sender);
    createTemplate(documentRequestEmailDto, variables, sender);
  }

  private Map<String, Object> getVariables(final DocumentRequestEmailDto documentRequestEmailDto,
      final User sender) {
    final String documentUrl = documentRequestEmailDto.getDocumentEmailUrl();
    final String documentTitle = documentRequestEmailDto.getDocumentTitle();
    final Map<String, Object> variables = new HashMap<>();

    variables.put("frontEndAddress", applicationConfig.getFrontEndAddress());
    variables.put("helpUrl", applicationConfig.getHelpUrl());

    final String senderName = UserNameUtil.getUserName(sender);
    final String senderAvatar =
        sender.getImageUrl() != null ? awsUtil.getAwsPath() + sender.getImageUrl()
            : applicationConfig.getFrontEndAddress() + "image/person.svg";

    variables.put("senderName", senderName);
    variables.put("senderAvatar", senderAvatar);
    variables.put("documentUrl", documentUrl);
    variables.put("documentTitle", documentTitle);

    if (!VIEW.equals(documentRequestEmailDto.getType())) {
      final Timestamp expireDate = documentRequestEmailDto.getExpiredAt();
      if (expireDate != null) {
        LocalDate dueDate = expireDate.toLocalDateTime().toLocalDate();
        variables.put("dueDate", DateUtil.formatDateTo(dueDate,"MMM dd"));
      }
    }

    return variables;
  }

  private void createTemplate(final DocumentRequestEmailDto documentRequestEmailDto,
      final Map<String, Object> variables, final User sender) {
    final String message = documentRequestEmailDto.getMessage();
    final DocumentRequestType type = documentRequestEmailDto.getType();
    final List<Long> recipientUserIds = documentRequestEmailDto.getRecipientUserIds();
    recipientUserIds.forEach(recipientUserId -> {
      final User recipienter = userRepository.findById(recipientUserId)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      if (SIGN.equals(type)) {
        final String template = "document_request_signature.html";
        variables.put("message", "\"" + message + "\"");
        final Email email = new Email(sender, recipienter, "Signature Request");
        sendEmail(variables, template, email);
      }

      if (ACKNOWLEDGE.equals(type)) {
        final String template = "document_request_acknowledge.html";
        variables.put("message", "\"" + message + "\"");
        final Email email = new Email(sender, recipienter, "Acknowledgement Request");
        sendEmail(variables, template, email);
      }

      if (VIEW.equals(type)) {
        final String template = "document_request_no_action.html";
        final String senderFirstName = sender.getUserPersonalInformation().getFirstName();
        final String subject = senderFirstName + " Sent You a Document";
        final Email email = new Email(sender, recipienter, subject);
        sendEmail(variables, template, email);
      }
    });


  }

  private void sendEmail(final Map<String, Object> variables, final String template,
      final Email email) {
    final String emailContent = templateEngine
        .process(template, new Context(Locale.ENGLISH, variables));
    email.setSendDate(new Timestamp(new Date().getTime()));
    email.setContent(emailContent);

    emailService.saveAndScheduleEmail(email);
  }
}
