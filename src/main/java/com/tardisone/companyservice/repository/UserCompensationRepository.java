package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.UserCompensation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;


public interface UserCompensationRepository extends BaseRepository<UserCompensation, Long>{
    @Transactional
    @Modifying
    @Query(
            value = "update user_compensations " +
                    " set wage= ?1 , " +
                    " compensation_frequency_id= ?2 " +
                    " where user_id = ?3 ",
            nativeQuery = true
    )
    void saveCompensatios(Integer wage,Long frequency,Long userId);

}
