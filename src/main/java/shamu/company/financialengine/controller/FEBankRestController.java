package shamu.company.financialengine.controller;

import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.financialengine.annotation.FinancialEngineRestController;
import shamu.company.financialengine.dto.BankAccountInfoDto;
import shamu.company.financialengine.dto.BankConnectionWidgetDto;
import shamu.company.financialengine.service.FEBankService;

/** @author Lucas */
@FinancialEngineRestController
public class FEBankRestController {
  private final FEBankService feBankService;

  public FEBankRestController(final FEBankService feBankService) {
    this.feBankService = feBankService;
  }

  @GetMapping("bank/connection-widget")
  public BankConnectionWidgetDto getCompanyBankConnect() {
    return feBankService.getCompanyBankConnection();
  }

  @GetMapping("bank/account-info")
  public BankAccountInfoDto getCompanyBankAccountInfo() {
    return feBankService.getCompanyBankAccountInfo();
  }
}
