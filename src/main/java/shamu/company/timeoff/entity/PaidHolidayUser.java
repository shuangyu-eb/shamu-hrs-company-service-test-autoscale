package shamu.company.timeoff.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "paid_holidays_users")
@NoArgsConstructor
@AllArgsConstructor
public class PaidHolidayUser extends BaseEntity {

  private Long companyId;

  private Long userId;

  private boolean isSelected;
}
