package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.model.JobUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobUserReposiory extends JpaRepository<JobUser, Integer> {
}
