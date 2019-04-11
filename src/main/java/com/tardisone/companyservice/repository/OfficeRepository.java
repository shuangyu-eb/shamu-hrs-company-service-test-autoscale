package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Office;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

@Repository
@Table(name = "offices")
public interface OfficeRepository extends BaseRepository<Office, Long>{
}
