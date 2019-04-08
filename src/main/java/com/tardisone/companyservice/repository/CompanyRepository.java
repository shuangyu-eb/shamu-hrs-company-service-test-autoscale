package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Company;

public interface CompanyRepository extends BaseRepository<Company, Long> {
    Boolean existsByName(String companyName);

    Boolean existsBySubdomainName(String subDomainName);
}
