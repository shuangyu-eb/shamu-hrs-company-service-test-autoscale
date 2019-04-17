package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.EmploymentType;
import com.tardisone.companyservice.repository.EmploymentTypeRepository;
import com.tardisone.companyservice.service.EmploymentTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmploymentTypeServiceImpl implements EmploymentTypeService {
    @Autowired
    EmploymentTypeRepository employmentTypeRepository;

    @Override
    public List<EmploymentType> getAllEmploymentType() {
        return employmentTypeRepository.findAll();
    }
}
