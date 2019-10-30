package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffRequestComment;

public interface TimeOffRequestCommentsRepository
    extends BaseRepository<TimeOffRequestComment, Long> {

  @Modifying
  @Query(value = "update TimeOffRequestComment tc set tc.deletedAt = current_timestamp "
      + "where tc.timeOffRequestId in ?1")
  void deleteByTimeOffRequestIds(List<Long> timeOffRequestIds);
}
