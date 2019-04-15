package shamu.company.user.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "martial_status")
@Where(clause = "deleted_at IS NULL")
@NoArgsConstructor
public class MaritalStatus extends BaseEntity {

    private String name;

    public MaritalStatus(Long id){
        this.setId(id);
    }
}
