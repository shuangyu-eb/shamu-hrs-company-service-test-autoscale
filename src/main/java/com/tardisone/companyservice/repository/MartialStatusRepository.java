package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.MaritalStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

@Repository
@Table(name = "martial_status")
public interface MartialStatusRepository extends BaseRepository<MaritalStatus, Long> {
}
