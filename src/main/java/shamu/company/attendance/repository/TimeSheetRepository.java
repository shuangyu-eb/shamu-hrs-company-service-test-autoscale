package shamu.company.attendance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.common.repository.BaseRepository;

import java.util.List;

public interface TimeSheetRepository extends BaseRepository<TimeSheet, String> {
  String QUERY_TEAM_TIMESHEETS_SQL =
      "select t.* from timesheets t "
          + " join users u on u.id = t.employee_id"
          + " join static_timesheet_status sts on sts.id = t.status_id"
          + " where "
          + "t.time_period_id = unhex(?1) "
          + " and u.company_id = unhex(?2) "
          + " and u.manager_user_id = unhex(?4) "
          + " and sts.name in (?3) order by t.updated_at desc ";

  String QUERY_COMPANY_TIMESHEETS_SQL =
      "select t.* from timesheets t "
          + " join users u on u.id = t.employee_id"
          + " join static_timesheet_status sts on sts.id = t.status_id"
          + " where "
          + "t.time_period_id = unhex(?1) "
          + " and u.company_id = unhex(?2) "
          + " and sts.name in (?3) order by t.updated_at desc ";

  @Query(
      value =
          "select t.* from timesheets t join users u  "
              + "on u.id = t.employee_id and u.company_id = unhex(?1)",
      nativeQuery = true)
  List<TimeSheet> listByCompanyId(String companyId);

  @Query(
      value = QUERY_TEAM_TIMESHEETS_SQL,
      countQuery = QUERY_TEAM_TIMESHEETS_SQL,
      nativeQuery = true)
  Page<TimeSheet> findTeamTimeSheetsByIdAndCompanyIdAndStatus(
      String timePeriodId, String companyId, String status, String userId, Pageable pageable);

  @Query(
      value = QUERY_COMPANY_TIMESHEETS_SQL,
      countQuery = QUERY_COMPANY_TIMESHEETS_SQL,
      nativeQuery = true)
  Page<TimeSheet> findCompanyTimeSheetsByIdAndCompanyIdAndStatus(
      String timePeriodId, String companyId, String status, String userId, Pageable pageable);

  @Query(value = QUERY_TEAM_TIMESHEETS_SQL, nativeQuery = true)
  List<TimeSheet> findTeamTimeSheetsByIdAndCompanyIdAndStatus(
      String timePeriodId, String companyId, List<String> status, String userId);

  @Query(value = QUERY_COMPANY_TIMESHEETS_SQL, nativeQuery = true)
  List<TimeSheet> findCompanyTimeSheetsByIdAndCompanyIdAndStatus(
      String timePeriodId, String companyId, List<String> status, String userId);

  boolean existsByEmployeeId(String employeeId);

  @Modifying
  @Transactional
  @Query(
      value = "update timesheets set status_id = unhex(?1) where id = unhex(?2)",
      nativeQuery = true)
  void updateTimesheetStatus(String statusId, String timesheetId);

  List<TimeSheet> findAllByTimePeriodId(String timePeriodId);

  TimeSheet findByTimePeriodIdAndEmployeeId(String periodId, String employeeId);

  @Query(
      value =
          "select t.* from timesheets t "
              + "join users u on u.id = t.employee_id "
              + "where t.time_period_id = unhex(?1) and hex(u.id) in (?2)",
      nativeQuery = true)
  List<TimeSheet> findAllByTimePeriodIdAndEmployeeId(String periodId, List<String> employeeIds);

  @Query(
      value =
          "select count(1) from timesheets t "
              + " join users u on u.id = t.employee_id"
              + " join static_timesheet_status sts on sts.id = t.status_id"
              + " where "
              + "t.time_period_id = unhex(?1) "
              + " and u.manager_user_id = unhex(?3) "
              + " and sts.name = ?2",
      nativeQuery = true)
  int findTeamHoursPendingCount(String timePeriodId, String status, String userId);

  @Query(
      value =
          "select count(1) from timesheets t "
              + " join users u on u.id = t.employee_id"
              + " join static_timesheet_status sts on sts.id = t.status_id"
              + " where "
              + "t.time_period_id = unhex(?1) "
              + " and sts.name = ?2",
      nativeQuery = true)
  int findCompanyHoursPendingCount(String timePeriodId, String status);
}
