package shamu.company.timeoff.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;

@Service
@Transactional
public class TimeOffPolicyUserService {

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  @Autowired
  public TimeOffPolicyUserService(final TimeOffPolicyUserRepository timeOffPolicyUserRepository) {
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
  }

  public TimeOffPolicyUser findById(final String id) {
    return timeOffPolicyUserRepository.findById(id)
        .orElseThrow(() ->
            new ResourceNotFoundException("Can not find time off policy user with id " + id));
  }
}