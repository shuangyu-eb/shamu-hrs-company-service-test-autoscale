package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.*;
import com.tardisone.companyservice.pojo.OfficeAddressPojo;
import com.tardisone.companyservice.repository.*;
import com.tardisone.companyservice.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    OfficeAddressRepository officeAddressRepository;

    @Autowired
    UserCompensationRepository userCompensationRepository;

    @Autowired
    EmploymentTypeRepository employmentTypeRepository;

    @Autowired
    OfficeRepository officeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JobUserRepository jobUserRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Override
    public Job findJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    @Override
    public List getAllByCompanyId(Long companyId) {
        return officeAddressRepository.getAllByCompanyId(companyId);
    }

    @Override
    public void saveCompensatios(Integer wage, Long frequency, Long userId) {
        userCompensationRepository.saveCompensatios(wage,frequency,userId);
    }

    @Override
    public void saveJob(String title, Long userId) {
        jobRepository.saveJob(title,userId);
    }

    @Override
    public EmploymentType findEmployTypeById(Long id) {
        return employmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employment not found"));
    }

    @Override
    public void saveOffice(Long officeAddressId, Long id) {
        officeRepository.saveOffice(officeAddressId,id);
    }

    @Override
    public void saveUserManager(Long managerId, Long id) {
        userRepository.saveUserManager(managerId,id);

    }

    @Override
    public void saveJobUser(Long employmentTypeId, Date startDate, Long userId) {
        jobUserRepository.saveJobUser(employmentTypeId,startDate,userId);
    }

    @Override
    public void saveEmploymentType(String name) {
        EmploymentType employmentType=new EmploymentType();
        employmentType.setName(name);
        employmentTypeRepository.save(employmentType);

    }

    @Override
    public void saveDepartment(String name, Long companyId) {
        Department department=new Department();
        department.setName(name);
        Company company=new Company();
        company.setId(companyId);
        department.setCompany(company);
        departmentRepository.save(department);

    }

    @Override
    public void saveOfficeAddress(OfficeAddressPojo addressPojo,Long companyId) {
        OfficeAddresses officeAddresses=new OfficeAddresses();
        officeAddresses.setStreet_1(addressPojo.getAddress());
        officeAddresses.setCity(addressPojo.getCity());
        StatesProvince statesProvince=new StatesProvince();
        statesProvince.setId(addressPojo.getState());
        officeAddresses.setStatesProvince(statesProvince);
        officeAddresses.setPostalCode(addressPojo.getZip());
        OfficeAddresses officeAddress=officeAddressRepository.save(officeAddresses);
        Office office=new Office();
        office.setName(addressPojo.getOfficeName());
        office.setOfficeAddress(officeAddress);
        Company company=new Company();
        company.setId(companyId);
        office.setCompany(company);
        officeRepository.save(office);


    }


}
