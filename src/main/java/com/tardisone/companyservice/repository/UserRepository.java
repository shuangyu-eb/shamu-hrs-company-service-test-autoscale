package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends BaseRepository<User, Long> {
    User findByEmailWork(String emailWork);

    User findByVerificationToken(String activationToken);

    @Query(value = "SELECT * FROM users WHERE manager_user_id IS NOT NULL AND deleted_at IS NULL", nativeQuery = true)
    List<User> findAllEmployees();

    @Query(value = "SELECT * FROM users WHERE user_role_id = 2 AND deleted_at IS NULL", nativeQuery = true)
    List<User> findAllManagers();
}
