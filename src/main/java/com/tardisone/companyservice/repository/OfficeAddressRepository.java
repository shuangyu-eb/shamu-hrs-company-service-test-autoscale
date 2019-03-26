package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Department;
import com.tardisone.companyservice.entity.OfficeAddresses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface OfficeAddressRepository extends JpaRepository<Department, Integer> {

    @Query(value = "SELECT id,name FROM offices where company_id=?1",
            nativeQuery = true)
    public List<Map> getAllByCompanyId(Long company_id);
}
