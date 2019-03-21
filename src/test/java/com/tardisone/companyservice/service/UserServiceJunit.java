package com.tardisone.companyservice.service;

import com.tardisone.companyservice.service.impl.UserServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", })
public class UserServiceJunit {

    @Autowired
    UserServiceImpl userService;

    @Test
    public void testGetActivationEmail() {
        String result = userService.getActivationEmail(UUID.randomUUID().toString());
        Assert.assertNotNull(result);
    }

    @Test
    public void testFinishUserVerification() {
        Boolean result = userService.finishUserVerification(UUID.randomUUID().toString());
        Assert.assertFalse(result);
    }
}
