package shamu.company.attendance.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;

@RestApiController
@Validated
public class AttendanceSetUpController extends BaseRestController {
  private final AttendanceSetUpService attendanceSetUpService;

  public AttendanceSetUpController(final AttendanceSetUpService attendanceSetUpService) {
    this.attendanceSetUpService = attendanceSetUpService;
  }

  @GetMapping("time-and-attendance/{userId}/is-attendance-set-up")
  @PreAuthorize("hasPermission(#userId, 'USER', 'VIEW_SELF')")
  public Boolean findIsAttendanceSetUp(@PathVariable String userId) {
    return attendanceSetUpService.findIsAttendanceSetUp(findCompanyId());
  }
}
