package shamu.company.user.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.CompensationOvertimeStatus;

import java.util.List;

public interface CompensationOvertimeStatusRepository
    extends BaseRepository<CompensationOvertimeStatus, String> {

  String SELECT_FROM_OVERTIME_POLICIES =
      "select id, policy_name as name, created_at, updated_at from overtime_policies ";

  @Override
  List<CompensationOvertimeStatus> findAll();

  CompensationOvertimeStatus findByName(String name);

  @Query(value = SELECT_FROM_OVERTIME_POLICIES + "where company_id = unhex(?1)", nativeQuery = true)
  List<CompensationOvertimeStatus> findByCompanyId(String companyId);

  @Query(
      value = SELECT_FROM_OVERTIME_POLICIES + "where company_id = unhex(?1) AND policy_name = ?2 ",
      nativeQuery = true)
  CompensationOvertimeStatus findByCompanyIdAndName(String companyId, String name);
}
