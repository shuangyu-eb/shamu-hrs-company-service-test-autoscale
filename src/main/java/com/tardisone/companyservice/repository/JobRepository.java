package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Job;


public interface JobRepository extends BaseRepository<Job, Long>{

    public Job findByTitle(String title);



}
