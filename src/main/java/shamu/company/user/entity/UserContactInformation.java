package shamu.company.user.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;

@Entity
@Data
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class UserContactInformation extends BaseEntity {

    private String phoneWork;

    private String phoneWorkExtension;

    private String phoneMobile;

    private String phoneHome;

    private String emailWork;

    private String emailHome;
}
