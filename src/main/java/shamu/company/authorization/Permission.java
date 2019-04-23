package shamu.company.authorization;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@Table(name = "permissions")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class Permission extends BaseEntity {

  @Basic(optional = false)
  @Enumerated(EnumType.STRING)
  private Name name;

  public enum Name {
    CREATE_USER(PermissionType.ADMIN_PERMISSION),
    DELETE_USER(PermissionType.ADMIN_PERMISSION),
    EDIT_USER(PermissionType.ADMIN_PERMISSION),
    VIEW_USER_TIME_OFF(PermissionType.MANAGER_PERMISSION),
    VIEW_USER_EMERGENCY_CONTACT(PermissionType.MANAGER_PERMISSION),
    VIEW_USER_CONTACT(PermissionType.EMPLOYEE_PERMISSION),
    VIEW_USER_PERSONAL(PermissionType.EMPLOYEE_PERMISSION),
    VIEW_USER_JOB(PermissionType.EMPLOYEE_PERMISSION),
    EDIT_SELF(PermissionType.SELF_PERMISSION);

    private PermissionType permissionType;
    Name(PermissionType permissionType) {
      this.permissionType = permissionType;
    }

    public PermissionType getPermissionType() {
      return permissionType;
    }
  }

  private Long permissionTypeId;

  public enum PermissionType {
    ADMIN_PERMISSION, MANAGER_PERMISSION, EMPLOYEE_PERMISSION, SELF_PERMISSION,
  }
}
