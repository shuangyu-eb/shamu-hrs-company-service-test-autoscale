package shamu.company.timeoff.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "time_off_policies")
@AllArgsConstructor
@NoArgsConstructor
public class TimeOffPolicy extends BaseEntity {

  private static final long serialVersionUID = 220495443223813321L;

  @Length(max = 50)
  private String name;

  private Boolean isLimited;

  private Boolean isAutoEnrollEnabled;

  public TimeOffPolicy(final String id) {
    setId(id);
  }
}
