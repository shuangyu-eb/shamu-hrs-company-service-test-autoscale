package shamu.company.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
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

    @OneToOne
    @JsonIgnore
    private User user;

    @OneToOne
    private CompensationType compensationType;

    @OneToOne
    private CompensationChangeReason compensationChangeReason;

    private String comment;
}
