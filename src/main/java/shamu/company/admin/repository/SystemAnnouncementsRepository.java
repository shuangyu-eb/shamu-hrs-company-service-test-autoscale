package shamu.company.admin.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.admin.entity.SystemAnnouncement;
import shamu.company.common.repository.BaseRepository;


public interface SystemAnnouncementsRepository extends BaseRepository<SystemAnnouncement, String> {

  @Query(
      value =
          "select * "
              + "from system_announcements sa "
              + "where sa.is_past_announcement is not true "
              + "order by sa.created_at desc limit 1 ",
          nativeQuery = true)
  SystemAnnouncement getSystemActiveAnnouncement();
}
