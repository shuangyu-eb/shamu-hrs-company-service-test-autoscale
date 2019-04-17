package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.Department;
import com.tardisone.companyservice.repository.DepartmentRepository;
import com.tardisone.companyservice.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    DepartmentRepository departmentRepository;

    @Override
    public List<Department> getAllDepartments(Long companyId) {
        return departmentRepository.getAllByCompanyId(companyId);
    }
}
