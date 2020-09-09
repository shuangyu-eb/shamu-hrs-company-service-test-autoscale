package shamu.company.job.entity;

import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.user.entity.EmployeeType;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;

@Entity
@Table(name = "jobs_users")
@Data
@NoArgsConstructor
public class JobUser extends BaseEntity {

  private static final long serialVersionUID = -9208546727354894506L;

  @OneToOne private User user;

  @ManyToOne private Job job;

  @ManyToOne private Department department;

  @ManyToOne private EmploymentType employmentType;

  @ManyToOne private EmployeeType employeeType;

  private Timestamp startDate;

  private Timestamp endDate;

  @ManyToOne private Office office;

  @OneToOne(cascade = CascadeType.ALL)
  private UserCompensation userCompensation;

  public JobUser(final User user, final Timestamp startDate) {
    this.user = user;
    this.startDate = startDate;
  }
}
