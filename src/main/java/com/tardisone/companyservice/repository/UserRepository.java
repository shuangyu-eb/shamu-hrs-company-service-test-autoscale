package com.tardisone.companyservice.repository;


import com.tardisone.companyservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {

  User findByEmailWork(String emailWork);

  @Query(value = "SELECT * FROM users WHERE  LOWER(?1) = LOWER(email_work)",
          nativeQuery = true)
  Optional<User> findByEmailIgnoreCase(String email);

  Optional<User> findByResetPasswordToken(String resetPasswordToken);

  Optional<User> findByVerificationToken(String verificationToken);

  Boolean existsByResetPasswordToken(String resetPasswordToken);

}
