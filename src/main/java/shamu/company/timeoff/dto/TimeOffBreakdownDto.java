package shamu.company.timeoff.dto;

import java.util.List;
import lombok.Data;
import org.springframework.util.CollectionUtils;

@Data
public class TimeOffBreakdownDto {

  private Integer balance;

  private Long untilDateInMillis;

  private List<TimeOffBreakdownItemDto> list;
  
  public void resetBalance() {
    if (!CollectionUtils.isEmpty(list)) {
      TimeOffBreakdownItemDto lastBreakdownItem = list.get(list.size() - 1);
      this.balance = lastBreakdownItem.getBalance();
    }
  }
}
