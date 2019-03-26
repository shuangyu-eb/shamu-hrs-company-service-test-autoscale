package com.tardisone.companyservice.repository;


import com.tardisone.companyservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {

  User findByEmailWork(String emailWork);

  @Query(value = "SELECT * FROM users WHERE  LOWER(?1) = LOWER(email_work)",
          nativeQuery = true)
  Optional<User> findByEmailIgnoreCase(String email);

  Optional<User> findByResetPasswordToken(String resetPasswordToken);

  Optional<User> findByVerificationToken(String verificationToken);

  Boolean existsByResetPasswordToken(String resetPasswordToken);

  @Query(value = "select u.id,p.first_name,p.last_name from users u\n" +
          "left join user_roles r\n" +
          "on u.user_role_id =r.id \n" +
          "left join user_personal_information p\n" +
          "on u.user_personal_information_id=p.id\n" +
          "where r.name like 'MANAGER'",
          nativeQuery = true)
  List<Map> getAllManager();

}
