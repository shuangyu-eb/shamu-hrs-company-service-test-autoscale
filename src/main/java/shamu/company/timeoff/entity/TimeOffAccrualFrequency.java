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
  @Transient
  private AccrualFrequencyType accrualFrequencyType;

  public TimeOffAccrualFrequency(Long id) {
    this.setId(id);
  }

  public enum AccrualFrequencyType {
    FREQUENCY_TYPE_ONE(1L),
    FREQUENCY_TYPE_TWO(2L),
    FREQUENCY_TYPE_THREE(3L);

    private Long value;

    AccrualFrequencyType(Long frequencyId) {
      this.value = frequencyId;
    }

    public Long getValue() {
      return this.value;
    }

    public void setValue(Long frequencyId) {
      this.value = frequencyId;
    }

    public boolean equalsTo(Long param) {
      return this.getValue() == param;
    }
  }
}
