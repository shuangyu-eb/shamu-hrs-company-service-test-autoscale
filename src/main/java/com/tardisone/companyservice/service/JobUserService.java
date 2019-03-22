package com.tardisone.companyservice.service;


import com.tardisone.companyservice.entity.Job;
import com.tardisone.companyservice.entity.JobUser;

import java.util.Optional;

public interface JobUserService {

  public JobUser findJobUserByUserId(Long userId);

  public void updateJobUser(JobUser job);


}
