package shamu.company.attendance.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "static_company_pay_frequency_types")
public class StaticCompanyPayFrequencyType extends BaseEntity {

  private String name;
}
