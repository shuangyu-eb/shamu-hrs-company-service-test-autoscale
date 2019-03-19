package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmailWork(String emailWork);
}
