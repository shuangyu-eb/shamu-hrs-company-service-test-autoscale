package shamu.company.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import shamu.company.admin.dto.SuperAdminUserDto;
import shamu.company.user.entity.User;

@Repository
public interface SuperAdminRepository extends JpaRepository<User, String> {

  @Query(
      value =
          "SELECT new shamu.company.admin.dto.SuperAdminUserDto(u) "
              + "FROM User u "
              + "WHERE u.userStatus.name in ?2 "
              + "AND ( "
              + "CONCAT(u.userPersonalInformation.firstName, ' ',"
              + " u.userPersonalInformation.lastName) "
              + "LIKE CONCAT('%',?1,'%') "
              + "OR "
              + "CONCAT(u.userPersonalInformation.preferredName, ' ', "
              + "u.userPersonalInformation.lastName) "
              + "LIKE CONCAT('%',?1,'%') "
              + "OR u.userContactInformation.emailWork LIKE CONCAT('%',?1,'%') )")
  Page<SuperAdminUserDto> getUsersByKeywordAndPageable(
      String keyword, String[] userStatus, Pageable pageable);
}
