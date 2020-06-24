package shamu.company.user.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.CompensationOvertimeStatus;

import java.util.List;

public interface CompensationOvertimeStatusRepository
    extends BaseRepository<CompensationOvertimeStatus, String> {

  @Override
  List<CompensationOvertimeStatus> findAll();

  CompensationOvertimeStatus findByName(String name);
}
