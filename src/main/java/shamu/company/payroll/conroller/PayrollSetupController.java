package shamu.company.payroll.conroller;

import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.payroll.dto.PayrollDetailDto;
import shamu.company.payroll.service.PayrollSetupService;

@RestApiController
public class PayrollSetupController {
  PayrollSetupService payrollSetupService;

  public PayrollSetupController(final PayrollSetupService payrollSetupService) {
    this.payrollSetupService = payrollSetupService;
  }

  @GetMapping("payroll/details")
  public PayrollDetailDto findPayrollDetails() {
    return payrollSetupService.getPayrollDetails();
  }
}
