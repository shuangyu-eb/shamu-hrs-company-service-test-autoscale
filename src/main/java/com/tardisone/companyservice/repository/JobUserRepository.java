package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.dto.JobUserDTO;
import com.tardisone.companyservice.entity.JobUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobUserRepository extends BaseRepository<JobUser, Long> {

    @Query(value ="SELECT u.id, u.email_work AS email, u.image_url AS imageUrl, up.first_name AS firstName, up.last_name AS lastName," +
    "c.name AS cityName, j.title AS jobTitle FROM users u " +
            "LEFT JOIN users m ON u.manager_user_id = m.id " +
            "LEFT JOIN user_personal_information up ON u.user_personal_information_id = up.id " +
            "LEFT JOIN user_addresses ua ON u.id = ua.user_id " +
            "LEFT JOIN cities c ON ua.city_id = c.id " +
            "LEFT JOIN jobs_users ju ON ju.user_id = u.id " +
            "LEFT JOIN jobs j ON ju.job_id = j.id " +
            "WHERE m.id = ?1 ORDER BY firstName, lastName ASC", nativeQuery = true)
    List<JobUserDTO> findAllEmployees(Long id);
}
