package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.User;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface EmployeesInformationRepository extends BaseRepository<User, Long> {

    User findByEmployeeNumber(String eid);

    @Query(value = "SELECT * FROM users WHERE manager_user_id = ?1", nativeQuery = true)
    List<User> findAllByManagerUserId(Long mid);
}
