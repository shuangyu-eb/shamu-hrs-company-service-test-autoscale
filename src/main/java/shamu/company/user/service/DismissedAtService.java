package shamu.company.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.user.entity.DismissedAt;
import shamu.company.user.repository.DismissedAtRepository;

@Service
@Transactional
public class DismissedAtService {

  private final DismissedAtRepository dismissedAtRepository;

  public DismissedAtService(final DismissedAtRepository dismissedAtRepository) {
    this.dismissedAtRepository = dismissedAtRepository;
  }

  public DismissedAt findByUserIdAndSystemAnnouncementId(final String userId, final String id) {
    return dismissedAtRepository.findByUserIdAndSystemAnnouncementId(userId, id);
  }

  public DismissedAt save(final DismissedAt dismissedAt) {
    return dismissedAtRepository.save(dismissedAt);
  }
}
