package shamu.company.job.service.impl;

import org.springframework.stereotype.Service;
import shamu.company.job.entity.JobUser;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.job.service.JobUserService;

@Service
public class JobUserServiceImpl implements JobUserService {

  private final JobUserRepository jobUserRepository;


  public JobUserServiceImpl(JobUserRepository jobUserRepository) {
    this.jobUserRepository = jobUserRepository;
  }

  @Override
  public JobUser getJobUserByUserId(Long userId) {
    return jobUserRepository.findByUserId(userId);
  }

  @Override
  public JobUser save(JobUser jobUser) {
    return jobUserRepository.save(jobUser);
  }

}
