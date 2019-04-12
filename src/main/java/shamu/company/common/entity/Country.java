package shamu.company.common.entity;

import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "countries")
@Where(clause = "deleted_at IS NULL")
public class Country extends BaseEntity {

    private String name;
}
