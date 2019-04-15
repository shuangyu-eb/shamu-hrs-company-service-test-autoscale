package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "genders")
@Where(clause = "deleted_at IS NULL")
public class Gender extends BaseEntity {

  private String name;
}
