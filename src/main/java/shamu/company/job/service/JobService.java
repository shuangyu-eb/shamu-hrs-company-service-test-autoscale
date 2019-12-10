package shamu.company.job.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.job.entity.Job;
import shamu.company.job.repository.JobRepository;

@Service
@Transactional
public class JobService {

  private final JobRepository jobRepository;

  @Autowired
  public JobService(final JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  public Job findById(final String id) {
    return jobRepository.findById(id)
       .orElseThrow(() ->
           new ResourceNotFoundException(String.format("Job with id %s not found!", id)));
  }
}
