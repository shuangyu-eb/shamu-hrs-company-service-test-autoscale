package shamu.company.attendance.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.attendance.dto.TimeAndAttendanceDetailsDto;
import shamu.company.attendance.dto.TimeAndAttendanceRelatedUserListDto;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.PayPeriodFrequencyService;
import shamu.company.common.BaseRestController;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.utils.ReflectionUtil;

import javax.validation.Valid;
import java.util.List;

@RestApiController
@Validated
public class AttendanceSetUpController extends BaseRestController {
  private final AttendanceSetUpService attendanceSetUpService;

  private final PayPeriodFrequencyService payPeriodFrequencyService;

  public AttendanceSetUpController(
      final AttendanceSetUpService attendanceSetUpService,
      final PayPeriodFrequencyService payPeriodFrequencyService) {
    this.attendanceSetUpService = attendanceSetUpService;
    this.payPeriodFrequencyService = payPeriodFrequencyService;
  }

  @GetMapping("time-and-attendance/{userId}/is-attendance-set-up")
  @PreAuthorize("hasPermission(#userId, 'USER', 'VIEW_SELF')")
  public Boolean findIsAttendanceSetUp(@PathVariable final String userId) {
    return attendanceSetUpService.findIsAttendanceSetUp(findCompanyId());
  }

  @GetMapping("time-and-attendance/users")
  public TimeAndAttendanceRelatedUserListDto getEmployees() {
    return attendanceSetUpService.getRelatedUsers(findCompanyId());
  }

  @GetMapping("time-and-attendance/pay-period-frequency")
  public List<CommonDictionaryDto> getAllPayPeriodFrequency() {
    final List<StaticCompanyPayFrequencyType> payPeriodFrequencies =
        payPeriodFrequencyService.findAll();
    return ReflectionUtil.convertTo(payPeriodFrequencies, CommonDictionaryDto.class);
  }

  @PostMapping("time-and-attendance/details")
  public HttpEntity createAttendanceDetails(
      @Valid @RequestBody final TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto) {
    final String companyId = findCompanyId();
    final String employeeId = findUserId();
    attendanceSetUpService.saveAttendanceDetails(timeAndAttendanceDetailsDto, companyId, employeeId);
    return new ResponseEntity(HttpStatus.OK);
  }
}
