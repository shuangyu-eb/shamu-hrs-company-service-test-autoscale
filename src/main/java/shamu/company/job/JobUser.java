package shamu.company.job;

import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.job.Job;
import shamu.company.employee.*;
import shamu.company.user.entity.User;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "jobs_users")
@Data
@Where(clause = "deleted_at IS NULL")
public class JobUser extends BaseEntity {

    @ManyToOne
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
    private Department department;

    @ManyToOne
    private Company company;

}
