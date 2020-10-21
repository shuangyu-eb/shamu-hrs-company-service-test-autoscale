package shamu.company.financialengine;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FinancialEngineResponse<T> {
  private Boolean success;

  private T body;

  private String error;
}
