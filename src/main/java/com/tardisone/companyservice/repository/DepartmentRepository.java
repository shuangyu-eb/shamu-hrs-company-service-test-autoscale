package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    public Department findByName(String name);

    public List<Department> getAllByCompanyId(Long companyId);


    @Transactional
    @Modifying
    @Query(
            value = "insert into departments(name,company_id) values (?1,?2) ",
            nativeQuery = true
    )
    public void saveDepartment(String name, Long companyId);


}
