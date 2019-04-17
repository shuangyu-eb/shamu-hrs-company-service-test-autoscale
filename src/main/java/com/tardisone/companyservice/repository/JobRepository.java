package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Job;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;


public interface JobRepository extends BaseRepository<Job, Long>{

    Job findByTitle(String title);

    @Transactional
    @Modifying
    @Query(
            value = "update jobs " +
                    " set title= ?1 " +
                    " where id = ?2 ",
            nativeQuery = true
    )
    void saveJob(String title,Long userId);



}
