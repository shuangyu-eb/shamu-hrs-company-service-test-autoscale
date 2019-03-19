package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.entity.UserPersonalInformation;
import com.tardisone.companyservice.pojo.Email;
import com.tardisone.companyservice.service.impl.SendEmailServiceImpl;
import com.tardisone.companyservice.utils.EmailUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(value = SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", })
public class SendEmailServiceJunit {
    @InjectMocks
    @Autowired
    SendEmailServiceImpl sendEmailService;

    @Test
    public void testInviteEmployee() {
        User employee = new User();
        UserPersonalInformation employeePersonalInfo = new UserPersonalInformation();
        employeePersonalInfo.setFirstName("Jack");
        employee.setUserPersonalInformation(employeePersonalInfo);

        User currentUser = new User();
        UserPersonalInformation currentUserInfo = new UserPersonalInformation();
        currentUserInfo.setFirstName("Max");
        currentUser.setUserPersonalInformation(currentUserInfo);

        Email email = new Email("418688784@qq.com", "zhaojian@easternbay.cn", "email test", null);
        Whitebox.setInternalState(sendEmailService, "serverAddress", "http://localhost:3000");

        ITemplateEngine templateEngine = PowerMockito.mock(ITemplateEngine.class);
        Whitebox.setInternalState(sendEmailService, "templateEngine", templateEngine);
        PowerMockito.when(templateEngine.process(anyString(), any(Context.class))).thenReturn("This is email content!");

        EmailUtil emailUtil = PowerMockito.mock(EmailUtil.class);
        Whitebox.setInternalState(sendEmailService, "emailUtil", emailUtil);
        PowerMockito.when(emailUtil.send(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        Boolean result = sendEmailService.inviteEmployee(employee, currentUser, email);
        Assert.assertTrue(result);
    }

    @Test
    public void testGetHtmlContent() {
        User employee = new User();
        UserPersonalInformation employeePersonalInfo = new UserPersonalInformation();
        employeePersonalInfo.setFirstName("Jack");
        employee.setUserPersonalInformation(employeePersonalInfo);

        User currentUser = new User();
        UserPersonalInformation currentUserInfo = new UserPersonalInformation();
        currentUserInfo.setFirstName("Max");
        currentUser.setUserPersonalInformation(currentUserInfo);

        String result = sendEmailService.getHtmlContent(employee, currentUser);
        Assert.assertNotNull(result);

        Pattern employeePattern = Pattern.compile("Hi\\s+(\\S+),");
        Matcher employeeMatcher = employeePattern.matcher(result);
        if (employeeMatcher.find()) {
            String employeeName = employeeMatcher.group(1);
            Assert.assertEquals(employeeName, employeePersonalInfo.getFirstName());
        }

        int index = result.indexOf("<p>" + currentUserInfo.getFirstName() + "</p>");
        Assert.assertNotEquals(index, -1);
    }
}
