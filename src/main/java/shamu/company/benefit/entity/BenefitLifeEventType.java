package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "benefit_life_event_types")
public class BenefitLifeEventType extends BaseEntity {

  private static final long serialVersionUID = -6477530226865780516L;
  private String name;

  @NoArgsConstructor
  public enum LifeEventType {
    BIRTH_ADOPTION_OF_CHILD("Birth/Adoption of Child"),
    MARRIAGE_DOMESTIC_PARTNERSHIP("Marriage/Domestic Partnership"),
    DIVORCE_DISSOLUTION_OF_DOMESTIC_PARTNERSHIP("Divorce/Dissolution of Domestic Partnership"),
    DEPENDENT_GAINS_LOSES_COVERAGE_FROM_ANOTHER_SOURCE(
        "Dependent Gains / Loses Coverage from another Source"),
    LOSS_OR_GAIN_OF_COVERAGE("Loss or Gain of Coverage");

    private String value;

    LifeEventType(final String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
