package shamu.company.user.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.CompensationOvertimeStatus;

public interface CompensationOvertimeStatusRepository
    extends BaseRepository<CompensationOvertimeStatus, String> {

  List<CompensationOvertimeStatus> findAll();
}
