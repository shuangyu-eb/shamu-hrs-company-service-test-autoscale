package shamu.company.timeoff.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.user.entity.User;

@Service
@Transactional
public class TimeOffPolicyUserService {

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  @Autowired
  public TimeOffPolicyUserService(final TimeOffPolicyUserRepository timeOffPolicyUserRepository) {
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
  }

  public TimeOffPolicyUser findById(final String id) {
    return timeOffPolicyUserRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Time off policy user with id %s not found!", id),
                    id,
                    "time off policy user"));
  }

  public boolean existsByUserId(final String userId) {
    return timeOffPolicyUserRepository.existsByUserId(userId);
  }

  public TimeOffPolicyUser findByUserAndTimeOffPolicy(
      final User user, final TimeOffPolicy timeOffPolicy) {
    return timeOffPolicyUserRepository.findTimeOffPolicyUserByUserAndTimeOffPolicy(
        user, timeOffPolicy);
  }
}
