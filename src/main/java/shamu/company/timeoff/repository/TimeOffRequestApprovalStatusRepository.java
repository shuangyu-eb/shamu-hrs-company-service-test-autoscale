package shamu.company.timeoff.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;

public interface TimeOffRequestApprovalStatusRepository
    extends BaseRepository<TimeOffRequestApprovalStatus, String> {

  List<TimeOffRequestApprovalStatus> findAll();

  TimeOffRequestApprovalStatus findByName(String name);
}
