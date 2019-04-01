package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.JobUser;
import com.tardisone.companyservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Date;

public interface JobUserRepository extends BaseRepository<JobUser, Long> {
    @Transactional
    @Modifying
    @Query(
            value = "update jobs_users " +
                    " set employment_type_id= ?1 , " +
                    " start_date= ?2 " +
                    " where user_id = ?3 ",
            nativeQuery = true
    )
    void saveJobUser(Long employmentTypeId, Date startDate, Long userId);

    JobUser findJobUserByUser(User user);
}
