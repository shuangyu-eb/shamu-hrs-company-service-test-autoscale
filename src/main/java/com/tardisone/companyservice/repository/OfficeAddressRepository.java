package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.OfficeAddress;
import org.springframework.stereotype.Repository;
import javax.persistence.Table;

@Repository
@Table(name = "office_addresses")
public interface OfficeAddressRepository extends BaseRepository<OfficeAddress, Long>{
}
