package shamu.company.payroll.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.financialengine.dto.CompanyTaxIdDto;
import shamu.company.payroll.dto.PayrollAuthorizedEmployeeDto;
import shamu.company.payroll.dto.PayrollDetailDto;
import shamu.company.payroll.dto.PayrollSetupEmployeeDto;
import shamu.company.payroll.service.PayrollSetUpService;

@RestApiController
public class PayrollSetUpRestController {

  private final PayrollSetUpService payrollSetUpService;

  public PayrollSetUpRestController(final PayrollSetUpService payrollSetUpService) {
    this.payrollSetUpService = payrollSetUpService;
  }

  @GetMapping("payroll/setup/employees")
  public List<PayrollSetupEmployeeDto> getPayrollSetUpEmployees() {
    return payrollSetUpService.getPayrollSetUpEmployees();
  }

  @GetMapping("payroll/setup/details")
  public PayrollDetailDto findPayrollDetails() {
    return payrollSetUpService.getPayrollDetails();
  }

  @GetMapping("payroll/required-tax-fields")
  public List<CompanyTaxIdDto> getTaxList() {
    return payrollSetUpService.getTaxList();
  }

  @GetMapping("payroll/setup/authorized-signers/employees")
  public Page<PayrollAuthorizedEmployeeDto> getPayrollAuthorizedSignerEmployees(
      final EmployeeListSearchCondition employeeListSearchCondition) {
    return payrollSetUpService.getPayrollAuthorizedEmployees(employeeListSearchCondition);
  }
}
