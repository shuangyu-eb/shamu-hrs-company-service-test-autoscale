package shamu.company.financialengine;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.utils.JsonUtil;

@Data
@NoArgsConstructor
public class FinancialEngineResponse<T> {
  private Boolean success;

  private T body;

  private String error;

  public T getBody(final Class<T> type) {
    return JsonUtil.formatToObject(this.body, type);
  }
}
