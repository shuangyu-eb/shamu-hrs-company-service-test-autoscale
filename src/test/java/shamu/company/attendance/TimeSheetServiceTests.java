package shamu.company.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.Timesheet;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;
import shamu.company.attendance.repository.TimesheetRepository;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.user.entity.UserCompensation;
import shamu.company.utils.UuidUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TimeSheetServiceTests {

  @InjectMocks TimeSheetService timeSheetService;

  @Mock private TimesheetRepository timesheetRepository;

  @Mock private StaticTimesheetStatusRepository staticTimesheetStatusRepository;

  @Mock TimePeriodService timePeriodService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class findTimeSheetById {
    Timesheet timesheet;

    @BeforeEach
    void init() {
      timesheet = new Timesheet();
    }

    @Test
    void whenIdExists_thenShouldSuccess() {
      Mockito.when(timesheetRepository.findById(Mockito.anyString()))
          .thenReturn(Optional.ofNullable(timesheet));
      assertThatCode(() -> timeSheetService.findTimeSheetById("1")).doesNotThrowAnyException();
    }

    @Test
    void whenIdNotExists_thenShouldThrow() {
      Mockito.when(timesheetRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> timeSheetService.findTimeSheetById("1"));
    }
  }

  @Nested
  class saveAll {
    List<Timesheet> timeSheetList;

    @BeforeEach
    void init() {
      timeSheetList = new ArrayList<>();
    }

    @Test
    void whenListValid_shouldSucceed() {
      Mockito.when(timesheetRepository.saveAll(timeSheetList)).thenReturn(Mockito.any());
      assertThatCode(() -> timeSheetService.saveAll(timeSheetList)).doesNotThrowAnyException();
    }
  }

  @Nested
  class findTeamTimeSheetsByIdAndCompanyIdAndStatus {
    String companyId;
    String timesheetId;
    TimeSheetStatus status;
    List<Timesheet> timeSheetList;
    Pageable pageable;
    Page<Timesheet> page;

    @BeforeEach
    void init() {
      companyId = UuidUtil.getUuidString();
      timesheetId = UuidUtil.getUuidString();
      status = TimeSheetStatus.ACTIVE;
      timeSheetList = new ArrayList<>();
      pageable = PageRequest.of(0, 2);
      page = new PageImpl<>(timeSheetList, pageable, timeSheetList.size());
    }

    @Test
    void whenNormal_thenShouldSuccess() {
      Mockito.when(
              timesheetRepository.findTeamTimeSheetsByIdAndStatus(
                  timesheetId, Collections.singletonList(status.getValue()), "1", pageable))
          .thenReturn(page);
      assertThatCode(
              () ->
                  timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      timesheetId, Collections.singletonList(status.getValue()), "1", pageable))
          .doesNotThrowAnyException();
    }

    @Test
    void findCompanyTimeSheetsByIdAndCompanyIdAndStatus() {
      final TimeSheetStatus timeSheetStatus = TimeSheetStatus.SUBMITTED;
      final Pageable pageable = PageRequest.of(0, 2);
      final Page<Timesheet> page = new PageImpl<>(Collections.emptyList());
      Mockito.when(
              timesheetRepository.findCompanyTimeSheetsByIdAndStatus(
                  "1", Collections.singletonList(timeSheetStatus.getValue()), pageable))
          .thenReturn(page);
      assertThatCode(
              () ->
                  timeSheetService.findCompanyTimeSheetsByIdAndStatus(
                      "1", Collections.singletonList(timeSheetStatus.getValue()), pageable))
          .doesNotThrowAnyException();
    }

    @Test
    void findTeamTimeSheetsByIdAndCompanyIdAndStatus() {
      final List<Timesheet> timesheets = new ArrayList<>();
      Mockito.when(
              timesheetRepository.findTeamTimeSheetsByIdAndStatus(
                  Mockito.anyString(), Mockito.anyList(), Mockito.any()))
          .thenReturn(timesheets);
      assertThatCode(
              () ->
                  timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      Mockito.anyString(), Mockito.any(), Mockito.anyList()))
          .doesNotThrowAnyException();
    }

    @Test
    void findCompanyTimeSheetsByCompanyIdAndStatus() {
      final List<Timesheet> timesheets = new ArrayList<>();
      Mockito.when(
              timesheetRepository.findCompanyTimeSheetsByIdAndStatus(
                  Mockito.anyString(), Mockito.anyList()))
          .thenReturn(timesheets);
      assertThatCode(
              () ->
                  timeSheetService.findCompanyTimeSheetsByIdAndStatus(
                      Mockito.anyString(), Mockito.any()))
          .doesNotThrowAnyException();
    }

    @Test
    void removeUserFromAttendance() {
      final List<String> userIds = new ArrayList<>();
      userIds.add("1");
      final TimePeriod timePeriod = new TimePeriod();
      timePeriod.setId("1");
      final List<Timesheet> timesheets = new ArrayList<>();
      final Timesheet timesheet = new Timesheet();
      timesheets.add(timesheet);
      Mockito.when(timePeriodService.findCompanyCurrentPeriod()).thenReturn(timePeriod);
      Mockito.when(timesheetRepository.findAllByTimePeriodIdAndEmployeeId("1", userIds))
          .thenReturn(timesheets);
      assertThatCode(() -> timeSheetService.removeUserFromAttendance(userIds))
          .doesNotThrowAnyException();
    }
  }

  @Test
  void updateTimesheetStatusByPeriodId() {
    assertThatCode(
            () ->
                timesheetRepository.updateTimesheetStatusByPeriodId(
                    Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .doesNotThrowAnyException();
  }

  @Test
  void updateCurrentOvertimePolicyByUser() {
    final Timesheet timesheet = new Timesheet();
    final UserCompensation userCompensation = new UserCompensation();
    userCompensation.setUserId("1");
    Mockito.when(timesheetRepository.findCurrentTimesheetByUser("1")).thenReturn(timesheet);
    assertThatCode(() -> timeSheetService.updateCurrentOvertimePolicyByUser(userCompensation))
        .doesNotThrowAnyException();
  }
}
