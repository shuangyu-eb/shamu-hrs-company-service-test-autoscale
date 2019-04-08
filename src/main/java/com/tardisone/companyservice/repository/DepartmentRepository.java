package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends BaseRepository<Department, Long> {

    public Department findByName(String name);
}
