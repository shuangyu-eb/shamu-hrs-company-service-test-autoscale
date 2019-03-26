package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    public Department findByName(String name);


    public List<Department> getAllByCompanyId(Long companyId);


}
