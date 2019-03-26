package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.UserStatus;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserStatusRepository extends BaseRepository<UserStatus, Long> {

    @Query(value = "SELECT * FROM user_statuses WHERE name IN ?1", nativeQuery = true)
    List<UserStatus> findAllByName(List<String> nameList);
}
