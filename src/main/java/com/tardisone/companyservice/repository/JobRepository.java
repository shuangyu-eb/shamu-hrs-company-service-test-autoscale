package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface JobRepository extends JpaRepository<Job, Integer>{

    public Job findByTitle(String title);

}
