package shamu.company.attendance;

import static org.assertj.core.api.Assertions.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.dto.BreakTimeLogDto;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.entity.EmployeeTimeEntry;
import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.attendance.repository.EmployeeTimeEntryRepository;
import shamu.company.attendance.repository.EmployeeTimeLogRepository;
import shamu.company.attendance.repository.StaticEmployeesTaTimeTypeRepository;
import shamu.company.attendance.service.AttendanceMyHoursService;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

public class AttendanceMyHoursServiceTests {
    @InjectMocks
    AttendanceMyHoursService attendanceMyHoursService;

    @Mock private EmployeeTimeEntryRepository employeeTimeEntryRepository;

    @Mock private EmployeeTimeLogRepository employeeTimeLogRepository;

    @Mock private StaticEmployeesTaTimeTypeRepository staticEmployeesTaTimeTypeRepository;

    @Mock private UserService userService;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Nested
    class saveTimeEntry{
        BreakTimeLogDto breakTimeLogDto;
        List<BreakTimeLogDto> breakTimeLogDtos;
        TimeEntryDto timeEntryDto;
        User user;
        EmployeeTimeEntry employeeTimeEntry;
        StaticEmployeesTaTimeType breakType;
        StaticEmployeesTaTimeType workType;

        @BeforeEach
        void init() {
            breakTimeLogDto = new BreakTimeLogDto();
            breakTimeLogDtos = new ArrayList<>();
            timeEntryDto = new TimeEntryDto();
            user = new User();
            employeeTimeEntry = new EmployeeTimeEntry();
            breakType = new StaticEmployeesTaTimeType();
            workType = new StaticEmployeesTaTimeType();
            breakType.setName(StaticEmployeesTaTimeType.TimeType.BREAK.name());
            workType.setName(StaticEmployeesTaTimeType.TimeType.WORK.name());
            breakTimeLogDto.setBreakStart(new Timestamp(new Date().getTime()));
            breakTimeLogDto.setBreakMin(30);
            breakTimeLogDto.setTimeType("BREAK");
            timeEntryDto.setStartTime(new Timestamp(new Date().getTime()));
            timeEntryDto.setHoursWorked(1);
            timeEntryDto.setMinutesWorked(1);
            Mockito.when(userService.findById(Mockito.anyString())).thenReturn(user);
            Mockito.when(employeeTimeEntryRepository.save(Mockito.any())).thenReturn(employeeTimeEntry);
            Mockito.when(staticEmployeesTaTimeTypeRepository.findByName(
                    StaticEmployeesTaTimeType.TimeType.BREAK.name())).thenReturn(breakType);
            Mockito.when(staticEmployeesTaTimeTypeRepository.findByName(
                    StaticEmployeesTaTimeType.TimeType.WORK.name())).thenReturn(workType);
        }

        @Test
        void whenNoBreak_thenShouldSuccess() {
            timeEntryDto.setBreakTimeLogs(breakTimeLogDtos);
            assertThatCode(
                    () -> attendanceMyHoursService.saveTimeEntry("1", timeEntryDto))
                    .doesNotThrowAnyException();
        }

        @Test
        void whenHasBreakButHasEndTime_thenShouldSuccess() {
            timeEntryDto.setEndTime(new Timestamp(new Date().getTime()));
            breakTimeLogDtos.add(breakTimeLogDto);
            timeEntryDto.setBreakTimeLogs(breakTimeLogDtos);
            assertThatCode(
                    () -> attendanceMyHoursService.saveTimeEntry("1", timeEntryDto))
                    .doesNotThrowAnyException();
        }
    }
}
