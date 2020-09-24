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
              timeSheetRepository.findTeamTimeSheetsByIdAndStatus(
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
      final Page<TimeSheet> page = new PageImpl<>(Collections.emptyList());
      Mockito.when(
              timeSheetRepository.findCompanyTimeSheetsByIdAndStatus(
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
      final List<TimeSheet> timeSheets = new ArrayList<>();
      Mockito.when(
              timeSheetRepository.findTeamTimeSheetsByIdAndStatus(
                  Mockito.anyString(), Mockito.anyList(), Mockito.any()))
          .thenReturn(timeSheets);
      assertThatCode(
              () ->
                  timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      Mockito.anyString(), Mockito.any(), Mockito.anyList()))
          .doesNotThrowAnyException();
    }

    @Test
    void findCompanyTimeSheetsByCompanyIdAndStatus() {
      final List<TimeSheet> timeSheets = new ArrayList<>();
      Mockito.when(
              timeSheetRepository.findCompanyTimeSheetsByIdAndStatus(
                  Mockito.anyString(), Mockito.anyList()))
          .thenReturn(timeSheets);
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
      final List<TimeSheet> timeSheets = new ArrayList<>();
      final TimeSheet timeSheet = new TimeSheet();
      timeSheets.add(timeSheet);
      Mockito.when(timePeriodService.findCompanyCurrentPeriod()).thenReturn(timePeriod);
      Mockito.when(timeSheetRepository.findAllByTimePeriodIdAndEmployeeId("1", userIds))
          .thenReturn(timeSheets);
      assertThatCode(() -> timeSheetService.removeUserFromAttendance(userIds))
          .doesNotThrowAnyException();
    }
  }

  @Test
  void updateTimesheetStatusByPeriodId() {
    assertThatCode(
            () ->
                timeSheetRepository.updateTimesheetStatusByPeriodId(
                    Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .doesNotThrowAnyException();
  }
}
