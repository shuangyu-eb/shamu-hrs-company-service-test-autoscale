package shamu.company.attendance.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.service.AttendanceMyHoursService;
import shamu.company.common.config.annotations.RestApiController;

@RestApiController
public class AttendanceMyHoursController {

  private final AttendanceMyHoursService attendanceMyHoursService;

  public AttendanceMyHoursController(AttendanceMyHoursService attendanceMyHoursService) {
    this.attendanceMyHoursService = attendanceMyHoursService;
  }

  @PostMapping("time-and-attendance/{userId}/add-time-entries")
  public void saveTimeEntry(
      @PathVariable final String userId,
      @RequestBody final TimeEntryDto timeEntryDto) {
      attendanceMyHoursService.saveTimeEntry(userId, timeEntryDto);
  }
}
