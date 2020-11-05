package shamu.company.financialengine.dto;

import java.util.List;
import lombok.Data;

@Data
public class BankAccountInfoDto {
  private BankAccountDto savedBankAccount;

  private List<BankConnectionDto> bankConnections;
}
