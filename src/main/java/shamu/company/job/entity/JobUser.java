package shamu.company.job.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Office;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.user.entity.User;

@Entity
@Table(name = "jobs_users")
@Data
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class JobUser extends BaseEntity {

  @OneToOne
  private User user;

  @ManyToOne
  private Job job;

  @ManyToOne
  private EmploymentType employmentType;

  private Timestamp startDate;

  private Timestamp endDate;

  @ManyToOne
  private Office office;

  @ManyToOne
  private Company company;

}
