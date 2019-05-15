package shamu.company.job.service;

import java.util.List;
import shamu.company.job.entity.JobUser;
import shamu.company.user.entity.User;

public interface JobUserService {

  JobUser getJobUserByUserId(Long userId);

  List getManagers(User user);

  JobUser save(JobUser jobUser);
}
