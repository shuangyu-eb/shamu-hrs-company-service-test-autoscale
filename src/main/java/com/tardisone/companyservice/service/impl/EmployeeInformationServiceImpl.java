package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.repository.EmployeesInformationRepository;
import com.tardisone.companyservice.service.EmployeeeInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeInformationServiceImpl implements EmployeeeInformationService {

    @Autowired
    EmployeesInformationRepository repository;

    @Override
    public User findEmployeeInfoByEmployeeNumber(String eid) {
        return repository.findByEmployeeNumber(eid);
    }

    @Override
    public List<User> findEmployeesByManagerId(Long mid) {
        return repository.findAllByManagerUserId(mid);
    }
}
