package shamu.company.info.entity;

import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "states")
@Where(clause = "deleted_at IS NULL")
public class State extends BaseEntity {

	private String name;
}
