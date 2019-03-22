package com.tardisone.jobservice;

import com.tardisone.companyservice.CompanyServiceApplication;
import com.tardisone.companyservice.entity.Job;
import com.tardisone.companyservice.entity.JobUser;
import com.tardisone.companyservice.service.JobService;
import com.tardisone.companyservice.service.JobUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class JobServiceTest {

    @Autowired
    JobService jobService;

    @Test
    public void findJobUser() {
        Integer jobId=1;
        Job job=jobService.findJobById(jobId.longValue());
        System.out.println(job);
    }


}
