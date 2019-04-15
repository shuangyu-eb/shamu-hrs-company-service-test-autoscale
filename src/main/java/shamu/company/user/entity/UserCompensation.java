package shamu.company.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.employee.entity.CompensationFrequency;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "user_compensations")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class UserCompensation extends BaseEntity {

    private Integer wage;

    private Timestamp startDate;

    private Timestamp endDate;

    private String overtimeStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    @OneToOne
    private CompensationType compensationType;

    @OneToOne
    private CompensationChangeReason compensationChangeReason;

    @OneToOne
    private CompensationFrequency compensationFrequency;

    private String comment;
}
