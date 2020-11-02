package shamu.company.job.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "compensation_frequency")
public class CompensationFrequency extends BaseEntity {

  private static final long serialVersionUID = 5071221824073053350L;
  private String name;

  public enum Frequency {
    PER_HOUR("Per Hour"),
    PER_WEEK("Per Week"),
    PER_MONTH("Per Month"),
    PER_YEAR("Per Year");
    private final String value;

    Frequency(final String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
