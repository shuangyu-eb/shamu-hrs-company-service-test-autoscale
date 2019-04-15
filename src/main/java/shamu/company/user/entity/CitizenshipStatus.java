package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "citizenship_statuses")
@Where(clause = "deleted_at IS NULL")
public class CitizenshipStatus extends BaseEntity {

  private String name;
}
