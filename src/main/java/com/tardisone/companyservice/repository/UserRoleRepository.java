package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.UserRole;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRoleRepository extends BaseRepository<UserRole, Long> {

    @Query(value = "SELECT * FROM user_roles WHERE name IN ?1 AND deleted_at IS NULL", nativeQuery = true)
    List<UserRole> findAllByName(List<String> nameList);
}
