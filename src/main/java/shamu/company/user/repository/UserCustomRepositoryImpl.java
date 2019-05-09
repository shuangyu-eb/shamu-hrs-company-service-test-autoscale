package shamu.company.user.repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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
  private EntityManager entityManager;

  @Override
  public Page<JobUserListItem> getAllByCondition(
      EmployeeListSearchCondition employeeListSearchCondition, Long companyId, Pageable pageable) {

    String countAllEmployees =
        "select count(1) from users u where u.deleted_at is null "
            + "and u.company_id = ?1";
    BigInteger employeeCount =
        (BigInteger) entityManager.createNativeQuery(countAllEmployees).setParameter(1, companyId)
            .getSingleResult();

    List<JobUserListItem> jobUserListItemList = new ArrayList<>();
    if (employeeCount.longValue() == 0) {
      return new PageImpl<>(jobUserListItemList, pageable, employeeCount.longValue());
    }

    StringBuilder getAllEmployeeSql = new StringBuilder(
        "select u.id as id, u.image_url as iamgeUrl, up.first_name as firstName, "
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
            + "and (up.first_name like concat('%', ?2, '%') "
            + "or up.last_name like concat('%', ?2, '%') "
            + "or d.name like concat('%', ?2, '%') or j.title like concat('%', ?2, '%')) ");

    getAllEmployeeSql.append("order by ");
    StringBuilder finalSql = getAllEmployeeSql;
    pageable.getSort().forEach(
        order -> finalSql.append(order.getProperty()).append(" ").append(order.getDirection())
            .append(","));
    int commaIndex = getAllEmployeeSql.lastIndexOf(",");
    getAllEmployeeSql = getAllEmployeeSql.replace(commaIndex, getAllEmployeeSql.length(), " ");
    getAllEmployeeSql.append("limit ").append(pageable.getPageNumber()).append(",")
        .append(pageable.getPageSize());

    Query employeeListQuery = entityManager.createNativeQuery(getAllEmployeeSql.toString());
    employeeListQuery.setParameter(1, companyId);
    employeeListQuery.setParameter(2, employeeListSearchCondition.getKeyword());
    List<?> jobUserList = employeeListQuery.getResultList();

    jobUserList.forEach(jobUser -> {
      if (jobUser instanceof Object[]) {
        Object[] jobUserItem = (Object[]) jobUser;
        JobUserListItem jobUserListItem = new JobUserListItem();
        jobUserListItem.setId(((BigInteger) jobUserItem[0]).longValue());
        jobUserListItem.setImageUrl((String) jobUserItem[1]);
        jobUserListItem.setFirstName((String) jobUserItem[2]);
        jobUserListItem.setLastName((String) jobUserItem[3]);
        jobUserListItem.setDepartment((String) jobUserItem[4]);
        jobUserListItem.setJobTitle((String) jobUserItem[5]);
        jobUserListItemList.add(jobUserListItem);
      }
    });

    return new PageImpl<>(jobUserListItemList, pageable, employeeCount.longValue());
  }
}
