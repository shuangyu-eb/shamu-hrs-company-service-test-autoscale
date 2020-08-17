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
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;
import shamu.company.attendance.repository.TimeSheetRepository;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.utils.UuidUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TimeSheetServiceTests {

  @InjectMocks TimeSheetService timeSheetService;

  @Mock private TimeSheetRepository timeSheetRepository;

  @Mock private StaticTimesheetStatusRepository staticTimesheetStatusRepository;

  @Mock TimePeriodService timePeriodService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class findTimeSheetById {
    TimeSheet timeSheet;

    @BeforeEach
    void init() {
      timeSheet = new TimeSheet();
    }

    @Test
    void whenIdExists_thenShouldSuccess() {
      Mockito.when(timeSheetRepository.findById(Mockito.anyString()))
          .thenReturn(Optional.ofNullable(timeSheet));
      assertThatCode(() -> timeSheetService.findTimeSheetById("1")).doesNotThrowAnyException();
    }

    @Test
    void whenIdNotExists_thenShouldThrow() {
      Mockito.when(timeSheetRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> timeSheetService.findTimeSheetById("1"));
    }
  }

  @Nested
  class saveAll {
    List<TimeSheet> timeSheetList;

    @BeforeEach
    void init() {
      timeSheetList = new ArrayList<>();
    }

    @Test
    void whenListValid_shouldSucceed() {
      Mockito.when(timeSheetRepository.saveAll(timeSheetList)).thenReturn(Mockito.any());
      assertThatCode(() -> timeSheetService.saveAll(timeSheetList)).doesNotThrowAnyException();
    }
  }

  @Nested
  class findTeamTimeSheetsByIdAndCompanyIdAndStatus {
    String companyId;
    String timesheetId;
    TimeSheetStatus status;
    List<TimeSheet> timeSheetList;
    Pageable pageable;
    Page<TimeSheet> page;

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
              timeSheetRepository.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                  timesheetId, companyId, status.getValue(), "1", pageable))
          .thenReturn(page);
      assertThatCode(
              () ->
                  timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      timesheetId, companyId, status, "1", pageable))
          .doesNotThrowAnyException();
    }

    @Test
    void whenCompanyIdValid_thenShouldSuccess() {
      final StaticTimesheetStatus submitStatus = new StaticTimesheetStatus();
      final TimePeriod timePeriod = new TimePeriod();
      timePeriod.setId("test_period_id");
      submitStatus.setName(TimeSheetStatus.SUBMITTED.name());
      Mockito.when(timePeriodService.findCompanyLastPeriod(companyId)).thenReturn(timePeriod);
      Mockito.when(staticTimesheetStatusRepository.findByName(Mockito.anyString()))
          .thenReturn(submitStatus);
      final TimeSheet timeSheet = new TimeSheet();
      assertThatCode(
              () ->
                  timeSheetService.updateCompanyLastPeriodTimeSheetsStatus(
                      companyId, TimeSheetStatus.SUBMITTED.name(), TimeSheetStatus.APPROVED.name()))
          .doesNotThrowAnyException();
    }
  }

  @Test
  void findCompanyTimeSheetsByIdAndCompanyIdAndStatus() {
    final TimeSheetStatus timeSheetStatus = TimeSheetStatus.SUBMITTED;
    final Pageable pageable = PageRequest.of(0, 2);
    final Page<TimeSheet> page = new PageImpl<>(Collections.emptyList());
    Mockito.when(
            timeSheetRepository.findCompanyTimeSheetsByIdAndCompanyIdAndStatus(
                "1", "1", timeSheetStatus.getValue(), "1", pageable))
        .thenReturn(page);
    assertThatCode(
            () ->
                timeSheetService.findCompanyTimeSheetsByIdAndCompanyIdAndStatus(
                    "1", "1", timeSheetStatus, "1", pageable))
        .doesNotThrowAnyException();
  }

  @Test
  void findTeamTimeSheetsByIdAndCompanyIdAndStatus() {
    final List<TimeSheet> timeSheets = new ArrayList<>();
    Mockito.when(
            timeSheetRepository.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                Mockito.anyString(), Mockito.any(), Mockito.anyList(), Mockito.any()))
        .thenReturn(timeSheets);
    assertThatCode(
            () ->
                timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                    Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.anyList()))
        .doesNotThrowAnyException();
  }

  @Test
  void findCompanyTimeSheetsByCompanyIdAndStatus() {
    final List<TimeSheet> timeSheets = new ArrayList<>();
    Mockito.when(
            timeSheetRepository.findCompanyTimeSheetsByIdAndCompanyIdAndStatus(
                Mockito.anyString(), Mockito.any(), Mockito.anyList(), Mockito.any()))
        .thenReturn(timeSheets);
    assertThatCode(
            () ->
                timeSheetService.findCompanyTimeSheetsByIdAndCompanyIdAndStatus(
                    Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.anyList()))
        .doesNotThrowAnyException();
  }
}
