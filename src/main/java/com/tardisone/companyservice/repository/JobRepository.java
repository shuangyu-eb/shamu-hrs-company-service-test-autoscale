package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends BaseRepository<Job, Long>{

    public Job findByTitle(String title);
}
