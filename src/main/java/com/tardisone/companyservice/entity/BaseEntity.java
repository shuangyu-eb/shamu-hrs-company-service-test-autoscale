package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.MappedSuperclass;
import java.sql.Timestamp;

@MappedSuperclass
@Data
public abstract class BaseEntity {

  private Timestamp deletedAt;

  public abstract Long getId();
}
