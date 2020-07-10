package shamu.company.user.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.UserCompensation;

import java.util.List;

public interface UserCompensationRepository extends BaseRepository<UserCompensation, String> {
  Boolean existsByUserId(String userId);

  UserCompensation findByUserId(String userId);

  @Query(
      value =
          "SELECT ucb.* FROM (SELECT max(uca.created_at) as created_at, uca.user_id as user_id "
              + "FROM user_compensations uca JOIN users u ON "
              + "u.company_id = unhex(?1) AND u.id = uca.user_id "
              + "JOIN compensation_overtime_statuses cos ON "
              + "uca.overtime_status_id = cos.id AND cos.name != ?2 "
              + "GROUP BY uca.user_id) as temp "
              + "JOIN user_compensations ucb "
              + "ON ucb.user_id = temp.user_id AND ucb.created_at = temp.created_at",
      nativeQuery = true)
  List<UserCompensation> listNewestEnrolledUserByCompanyId(String companyId, String overtimeStatus);
}
