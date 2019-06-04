package shamu.company.job.service;

import shamu.company.job.entity.JobUser;

public interface JobUserService {

  JobUser getJobUserByUserId(Long userId);

  JobUser save(JobUser jobUser);
}
