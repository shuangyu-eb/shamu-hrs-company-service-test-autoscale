package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.entity.UserPersonalInformation;
import com.tardisone.companyservice.pojo.Email;
import com.tardisone.companyservice.service.SendEmailService;
import com.tardisone.companyservice.utils.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class SendEmailServiceImpl implements SendEmailService {

    @Autowired
    ITemplateEngine templateEngine;

    @Value("${application.address}")
    private String serverAddress;

    @Autowired
    EmailUtil emailUtil;

    @Override
    public Boolean inviteEmployee(User employee, User currentUser, Email email) {
        String htmlContent = getHtmlContent(employee, currentUser);
        return emailUtil.send(email.getFrom(), email.getTo(), email.getSubject(), htmlContent);
    }

    public String getHtmlContent(User employee, User currentUser) {
        Context context = new Context();
        context.setVariable("createPasswordUrl", serverAddress);

        UserPersonalInformation employeePersonalInfo = employee.getUserPersonalInformation();
        context.setVariable("firstName", employeePersonalInfo.getFirstName());

        UserPersonalInformation userPersonalInformation = currentUser.getUserPersonalInformation();
        context.setVariable("username", userPersonalInformation.getFirstName());

        return templateEngine.process("employee-invitation.html", context);
    }
}
