package shamu.company.employee.entity;

import com.alibaba.fastjson.annotation.JSONField;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "employment_types")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EmploymentType extends BaseEntity {

  @Length(max = 30)
  private String name;

  @ManyToOne
  @JSONField(serialize = false)
  private Company company;
}
