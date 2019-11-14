package shamu.company.info.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "states")
public class State extends BaseEntity {

  private String name;

  public State(String id) {
    this.setId(id);
  }
}
