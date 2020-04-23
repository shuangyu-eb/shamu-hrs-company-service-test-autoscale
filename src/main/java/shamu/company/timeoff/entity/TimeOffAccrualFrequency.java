package shamu.company.timeoff.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "time_off_accrual_frequencies")
@NoArgsConstructor
public class TimeOffAccrualFrequency extends BaseEntity {

  private String name;
  @Transient private AccrualFrequencyType accrualFrequencyType;

  public TimeOffAccrualFrequency(String id) {
    this.setId(id);
  }

  public enum AccrualFrequencyType {
    FREQUENCY_TYPE_ONE("All at once (beginning of each year)"),
    FREQUENCY_TYPE_TWO("All at once (each anniversary date)"),
    FREQUENCY_TYPE_THREE("Throughout the year (at the start of each month)");

    private String value;

    AccrualFrequencyType(String value) {
      this.value = value;
    }

    public String getValue() {
      return this.value;
    }

    public boolean equalsTo(String param) {
      return this.getValue().equals(param);
    }
  }
}
