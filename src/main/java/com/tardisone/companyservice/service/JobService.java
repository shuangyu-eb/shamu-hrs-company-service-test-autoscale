package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.EmploymentType;
import com.tardisone.companyservice.entity.Job;
import com.tardisone.companyservice.entity.Office;
import com.tardisone.companyservice.entity.OfficeAddresses;
import com.tardisone.companyservice.pojo.OfficeAddressPojo;

import java.util.Date;
import java.util.List;

public interface JobService {
    Job findJobById(Long id);

    List getAllByCompanyId(Long companyId);

    void saveCompensatios(Integer wage,Long frequency,Long userId);

    void saveJob(String title,Long userId);

    EmploymentType findEmployTypeById(Long id);

    void saveOffice(Long officeAddressId,Long id);

    void saveUserManager(Long managerId, Long id);

    void saveJobUser(Long employmentTypeId, Date startDate, Long userId);

    void saveEmploymentType(String name);

    void saveDepartment(String name,Long companyId);

    void saveOfficeAddress(OfficeAddressPojo addressPojo,Long companyId);



}
