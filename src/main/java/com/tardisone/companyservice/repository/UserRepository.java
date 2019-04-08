package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.User;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends BaseRepository<User, Long> {
    User findByEmailWork(String emailWork);

    User findByVerificationToken(String activationToken);

    @Query(value = "SELECT * FROM users WHERE manager_user_id IS NOT NULL AND deleted_at IS NULL", nativeQuery = true)
    List<User> findAllEmployees();

    Boolean existsByEmailWork(String email);
}
