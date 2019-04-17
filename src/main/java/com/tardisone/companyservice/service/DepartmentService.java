package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.Department;
import java.util.List;
import java.util.Map;

public interface DepartmentService {
    public List<Department> getAllDepartments(Long companyId);
}
