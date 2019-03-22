package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.JobUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobUserRepository extends JpaRepository<JobUser, Integer> {

    JobUser findByUserId(Long userId);

}
