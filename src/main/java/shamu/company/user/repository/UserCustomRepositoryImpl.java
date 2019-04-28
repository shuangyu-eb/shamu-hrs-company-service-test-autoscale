package shamu.company.user.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.entity.JobUserListItem;

@Repository
public class UserCustomRepositoryImpl implements UserCustomRepository {

  @PersistenceContext
  EntityManager entityManager;

  @Override
  public Page<JobUserListItem> getAllByCondition(
      EmployeeListSearchCondition employeeListSearchCondition, Long companyId, Pageable pageable) {

    String countAllEmployees = "select count(1) from users u where u.deleted_at is null "
        + "and u.company_id = " + companyId + ";";
    BigInteger employeeCount = (BigInteger) entityManager.createNativeQuery(countAllEmployees)
        .getSingleResult();

    String sql = "select u.id as id, u.image_url as iamgeUrl, up.first_name as firstName, "
        + "up.last_name as lastName, d.name as department, j.title as jobTitle "
        + "from users u "
        + "left join  user_personal_information up on u.user_personal_information_id = up.id "
        + "left join jobs_users ju on u.id = ju.user_id "
        + "left join jobs j on ju.job_id = j.id "
        + "left join departments d on ju.department_id = d.id "
        + "where u.deleted_at is null "
        + "and ju.deleted_at is null "
        + "and j.deleted_at is null "
        + "and u.company_id = ?1 "
        + "and (up.first_name like concat('%', ?2, '%') or up.last_name like concat('%', ?2, '%') "
        + "or d.name like concat('%', ?2, '%') or j.title like concat('%', ?2, '%')) "
        + "order by " + employeeListSearchCondition.getSortField().getSortValue() + " "
        + employeeListSearchCondition
        .getSortDirection() + " limit " + employeeListSearchCondition.getPage()
        + "," + employeeListSearchCondition.getSize() + ";";
    Query employeeListQuery = entityManager.createNativeQuery(sql);
    employeeListQuery.setParameter(1, companyId);
    employeeListQuery.setParameter(2, employeeListSearchCondition.getKeyword());
    List<Object[]> jobUserList = employeeListQuery.getResultList();

    List<JobUserListItem> jobUserListItemList = jobUserList.stream().map(jobUser -> {
      JobUserListItem jobUserListItem = new JobUserListItem();
      jobUserListItem.setId(((BigInteger) jobUser[0]).longValue());
      jobUserListItem.setImageUrl((String) jobUser[1]);
      jobUserListItem.setFirstName((String) jobUser[2]);
      jobUserListItem.setLastName((String) jobUser[3]);
      jobUserListItem.setDepartment((String) jobUser[4]);
      jobUserListItem.setJobTitle((String) jobUser[5]);
      return jobUserListItem;
    }).collect(Collectors.toList());
    return new PageImpl(jobUserListItemList, pageable, employeeCount.longValue());
  }
}
