package com.tardisone.companyservice.service;

public interface CompanyService {
    Boolean existsByName(String companyName);

    Boolean existsBySubdomainName(String subDomainName);
}
