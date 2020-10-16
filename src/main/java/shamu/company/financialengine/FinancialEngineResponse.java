package shamu.company.financialengine;

import lombok.Data;

@Data
public class FinancialEngineResponse<T> {
  private Boolean success;

  private T body;

  private String error;
}
