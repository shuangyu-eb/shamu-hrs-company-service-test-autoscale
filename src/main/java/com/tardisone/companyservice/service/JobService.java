package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.Job;
import com.tardisone.companyservice.entity.OfficeAddresses;

import java.util.List;

public interface JobService {
    Job findJobById(Long id);

    public List getAllByCompanyId(Long companyId);
}
