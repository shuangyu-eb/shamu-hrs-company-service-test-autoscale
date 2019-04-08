package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.JobUser;
import com.tardisone.companyservice.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobUserRepository extends BaseRepository<JobUser, Long> {

    List<JobUser> findAllByUserIn(List<User> users);
}
