package shamu.company.attendance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.common.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface TimeSheetRepository extends BaseRepository<TimeSheet, String> {
  String QUERY_TEAM_TIMESHEETS_SQL =
      "select t.* from timesheets t "
          + " join users u on u.id = t.employee_id"
          + " join static_timesheet_status sts on sts.id = t.status_id"
          + " where "
          + "t.time_period_id = unhex(?1) "
          + " and u.manager_user_id = unhex(?3) "
          + " and sts.name in (?2) order by t.updated_at desc ";

  String QUERY_COMPANY_TIMESHEETS_SQL =
      "select t.* from timesheets t "
          + " join static_timesheet_status sts on sts.id = t.status_id"
          + " where "
          + "t.time_period_id = unhex(?1) "
          + " and sts.name in (?2) order by t.updated_at desc ";

  @Query(
      value = QUERY_TEAM_TIMESHEETS_SQL,
      countQuery = QUERY_TEAM_TIMESHEETS_SQL,
      nativeQuery = true)
  Page<TimeSheet> findTeamTimeSheetsByIdAndStatus(
      String timePeriodId, List<String> status, String userId, Pageable pageable);

  @Query(
      value = QUERY_COMPANY_TIMESHEETS_SQL,
      countQuery = QUERY_COMPANY_TIMESHEETS_SQL,
      nativeQuery = true)
  Page<TimeSheet> findCompanyTimeSheetsByIdAndStatus(
      String timePeriodId, List<String> status, Pageable pageable);

  @Query(value = QUERY_TEAM_TIMESHEETS_SQL, nativeQuery = true)
  List<TimeSheet> findTeamTimeSheetsByIdAndStatus(
      String timePeriodId, List<String> status, String userId);

  @Query(value = QUERY_COMPANY_TIMESHEETS_SQL, nativeQuery = true)
  List<TimeSheet> findCompanyTimeSheetsByIdAndStatus(String timePeriodId, List<String> status);

  boolean existsByEmployeeId(String employeeId);

  @Modifying
  @Transactional
  @Query(
      value = "update timesheets set status_id = unhex(?1) where id = unhex(?2)",
      nativeQuery = true)
  void updateTimesheetStatus(String statusId, String timesheetId);

  @Modifying
  @Transactional
  @Query(
      value =
          "update timesheets set status_id = unhex(?2) where status_id = unhex(?1) and "
              + "time_period_id = unhex(?3)",
      nativeQuery = true)
  void updateTimesheetStatusByPeriodId(String fromStatus, String toStatus, String periodId);

  @Modifying
  @Transactional
  @Query(
      value =
          "update timesheets ts set ts.status_id = unhex(?2) where ts.status_id = unhex(?1) and "
              + "ts.time_period_id = unhex(?3) and ts.employee_id in "
              + "(select u.id from users u where u.manager_user_id = unhex(?4))",
      nativeQuery = true)
  void updateTimesheetStatusByPeriodIdAndManagerId(
      String fromStatus, String toStatus, String periodId, String managerId);

  List<TimeSheet> findAllByTimePeriodId(String timePeriodId);

  TimeSheet findByTimePeriodIdAndEmployeeIdAndRemovedAtIsNull(String periodId, String employeeId);

  Optional<TimeSheet> findByTimePeriodIdAndEmployeeId(String periodId, String employeeId);

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

  @Query(
      value =
          "select t.* from timesheets t "
              + "join time_period tp on tp.id = t.time_period_id "
              + "where tp.end_date > now() "
              + "AND t.user_compensation_id = UNHEX(?1)",
      nativeQuery = true)
  TimeSheet findCurrentRecordByUserCompensationId(String compensationId);

  @Query(
      value =
          "select timesheets.* FROM timesheets "
              + "join time_period "
              + "on timesheets.time_period_id = time_period.id "
              + "where employee_id = unhex(?1) "
              + "and time_period.start_date<current_timestamp "
              + "and time_period.end_date>current_timestamp",
      nativeQuery = true)
  TimeSheet findCurrentTimesheetByUser(String userId);
}
