package shamu.company.user.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "user_roles")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class UserRole extends BaseEntity {

    public UserRole(String name) {
        this.name = name;
    }

    private String name;
}
