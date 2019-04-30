package shamu.company.timeoff.paidholiday;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "paid_holidays")
@NoArgsConstructor
@AllArgsConstructor
public class PaidHoliday extends BaseEntity {
  private Long companyId;

  private String name;

  private Boolean isCustom = true;

  private Date holidayDate;

  private Boolean isSelect = false;
}
