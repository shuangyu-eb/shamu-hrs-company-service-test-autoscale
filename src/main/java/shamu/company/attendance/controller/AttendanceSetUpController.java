package shamu.company.attendance.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.common.BaseRestController;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.service.PayPeriodFrequencyService;
import shamu.company.utils.ReflectionUtil;

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
  public Boolean findIsAttendanceSetUp(@PathVariable String userId) {
    return attendanceSetUpService.findIsAttendanceSetUp(findCompanyId());
  }

  @GetMapping("pay-period-frequency")
  public List<CommonDictionaryDto> getAllPayPeriodFrequency() {
    final List<StaticCompanyPayFrequencyType> payPeriodFrequencies = payPeriodFrequencyService.findAll();
    return ReflectionUtil.convertTo(payPeriodFrequencies, CommonDictionaryDto.class);
  }
}
