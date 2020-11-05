package shamu.company.financialengine.dto;

import java.util.List;
import lombok.Data;

@Data
public class BankConnectionDto {
  private String name;

  private String connectionStatus;

  private String connectionStatusText;

  private Boolean isBeingAggregated;

  private String successfullyAggregatedAt;

  private List<BankAccountDto> availableAccounts;
}
