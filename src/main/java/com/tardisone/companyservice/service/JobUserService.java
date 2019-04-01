package com.tardisone.companyservice.service;


import com.tardisone.companyservice.entity.JobUser;
import com.tardisone.companyservice.entity.User;

public interface JobUserService {

  public JobUser findJobUserByUser(User user);

  public void updateJobUser(JobUser job);


}
