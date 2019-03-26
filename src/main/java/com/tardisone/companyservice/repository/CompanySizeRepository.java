package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.CompanySize;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompanySizeRepository extends BaseRepository<CompanySize, Long> {

    @Query(value = "SELECT * FROM company_sizes WHERE name IN ?1", nativeQuery = true)
    List<CompanySize> findAllByName(List<String> nameList);
}
