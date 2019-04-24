package shamu.company.job.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import shamu.company.common.repository.BaseRepository;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.entity.User;

@Repository
public interface JobUserRepository extends BaseRepository<JobUser, Long> {

  @Query(value = "SELECT * FROM jobs_users WHERE user_id=?1 AND deleted_at IS NULL ",
      nativeQuery = true)
  JobUser findByUserId(Long userId);

  List<JobUser> findAllByUserIn(List<User> users);

  JobUser findJobUserByUser(User user);

  @Query(
      value =
          "SELECT new shamu.company.job.entity.JobUserListItem("
              + "ju.id, u.id, u.imageUrl, up.firstName, up.lastName, d.name, j.title) "
              + "FROM JobUser ju "
              + "LEFT JOIN ju.user u "
              + "LEFT JOIN u.company c "
              + "LEFT JOIN u.userPersonalInformation up "
              + "LEFT JOIN ju.job j "
              + "LEFT JOIN ju.department d "
              + "where ju.deletedAt IS NULL AND u.deletedAt IS NULL "
              + "AND c.id = :companyId "
              + "AND (up.firstName "
              + "LIKE concat('%', :#{#employeeListSearchCondition.keyword}, '%') "
              + "OR up.lastName "
              + "LIKE concat('%', :#{#employeeListSearchCondition.keyword}, '%') "
              + "OR d.name LIKE "
              + "concat('%', :#{#employeeListSearchCondition.keyword}, '%') "
              + "OR j.title LIKE "
              + "concat('%', :#{#employeeListSearchCondition.keyword}, '%')) ")
  Page<JobUserListItem> getAllByCondition(
      @Param("employeeListSearchCondition") EmployeeListSearchCondition employeeListSearchCondition,
      @Param("companyId") Long companyId,
      Pageable pageable);
}
