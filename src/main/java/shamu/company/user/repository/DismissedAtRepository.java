package shamu.company.user.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.DismissedAt;

public interface DismissedAtRepository extends BaseRepository<DismissedAt, String> {

  DismissedAt findByUserIdAndSystemAnnouncementId(String userId, String id);
}
