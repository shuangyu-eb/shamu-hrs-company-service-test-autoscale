package shamu.company.job.service;

import shamu.company.job.entity.JobUser;
import shamu.company.user.entity.User;

public interface JobUserService {

  JobUser getJobUserByUserId(Long userId);

  JobUser getJobUserByUser(User user);

  JobUser save(JobUser jobUser);
}
