package shamu.company.timeoff.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "paid_holidays_users")
@NoArgsConstructor
@AllArgsConstructor
public class PaidHolidayUser extends BaseEntity {

  private static final long serialVersionUID = 5566794448110573459L;

  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String userId;

  private boolean isSelected;
}
