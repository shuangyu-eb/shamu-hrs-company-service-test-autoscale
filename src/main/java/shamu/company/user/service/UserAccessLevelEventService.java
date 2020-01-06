package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.user.entity.UserAccessLevelEvent;
import shamu.company.user.repository.UserAccessLevelEventRepository;

@Service
public class UserAccessLevelEventService {

  private final UserAccessLevelEventRepository userAccessLevelEventRepository;

  @Autowired
  public UserAccessLevelEventService(
          final UserAccessLevelEventRepository userAccessLevelEventRepository) {
    this.userAccessLevelEventRepository = userAccessLevelEventRepository;
  }

  public UserAccessLevelEvent save(final UserAccessLevelEvent userAccessLevelEvent) {
    return userAccessLevelEventRepository.save(userAccessLevelEvent);
  }
}
