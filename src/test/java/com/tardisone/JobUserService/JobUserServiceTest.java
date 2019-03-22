package com.tardisone.JobUserService;

import com.tardisone.companyservice.CompanyServiceApplication;
import com.tardisone.companyservice.entity.JobUser;
import com.tardisone.companyservice.repository.JobUserRepository;
import com.tardisone.companyservice.service.JobUserService;
import com.tardisone.companyservice.service.impl.JobUserServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = CompanyServiceApplication.class)
@ActiveProfiles("local")
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", })
public class JobUserServiceTest {

    @Autowired
    JobUserService jobUserService;

    @Test
    public void generateInboxMessage() {
        Integer userId=1;
        JobUser jobUser=jobUserService.findJobUserByUserId(userId.longValue());
        System.out.println(jobUser);
    }


}
