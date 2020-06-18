package shamu.company.attendance.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.attendance.dto.MyHoursListDto;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.service.AttendanceMyHoursService;
import shamu.company.common.config.annotations.RestApiController;

@RestApiController
public class AttendanceMyHoursController {

  private final AttendanceMyHoursService attendanceMyHoursService;

  public AttendanceMyHoursController(final AttendanceMyHoursService attendanceMyHoursService) {
    this.attendanceMyHoursService = attendanceMyHoursService;
  }

  @PostMapping("time-and-attendance/{userId}/add-time-entries")
  public void saveTimeEntry(
      @PathVariable final String userId, @RequestBody final TimeEntryDto timeEntryDto) {
    attendanceMyHoursService.saveTimeEntry(userId, timeEntryDto);
  }

  @GetMapping("time-and-attendance/total-paid-time/{timesheetId}")
  public List<MyHoursListDto> findMyHoursList(@PathVariable final String timesheetId) {
    return attendanceMyHoursService.findMyHoursLists(timesheetId);
  }
}
