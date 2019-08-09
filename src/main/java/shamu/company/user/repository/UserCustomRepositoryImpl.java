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
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.entity.User;

@Repository
public class UserCustomRepositoryImpl implements UserCustomRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Page<JobUserListItem> getAllByCondition(
          EmployeeListSearchCondition employeeListSearchCondition, Long companyId,
          Pageable pageable, Boolean isAdmin) {

    String countAllEmployees = "";
    if ((null != isAdmin && !isAdmin) || !employeeListSearchCondition.isSearched()) {
      countAllEmployees =
              "select count(1) from users u where u.deleted_at is null "
                      + " and u.deactivated_at is null and u.company_id = ?1 ";
    } else {
      countAllEmployees =
              "select count(1) from users u where u.deleted_at is null "
                      + "and u.company_id = ?1";
    }
    BigInteger employeeCount =
        (BigInteger)
            entityManager
                .createNativeQuery(countAllEmployees)
                .setParameter(1, companyId)
                .getSingleResult();

    List<JobUserListItem> jobUserItemList = new ArrayList<>();
    if (employeeCount.longValue() == 0) {
      return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
    }

    String originalSql =
        "select u.id as id, u.image_url as iamgeUrl, up.first_name as firstName, "
            + "up.last_name as lastName, d.name as department, j.title as jobTitle "
            + "from users u "
            + "left join  user_personal_information up on u.user_personal_information_id = up.id "
            + "left join jobs_users ju on u.id = ju.user_id "
            + "left join jobs j on ju.job_id = j.id "
            + "left join departments d on j.department_id = d.id "
            + "where u.deleted_at is null "
            + "and ju.deleted_at is null "
            + "and j.deleted_at is null "
            + "and u.company_id = ?1 "
            + "and (up.first_name like concat('%', ?2, '%') "
            + "or up.last_name like concat('%', ?2, '%') "
            + "or d.name like concat('%', ?2, '%') or j.title like concat('%', ?2, '%')) ";
    String additionalSql = " and u.deactivated_at is null ";

    String resultSql = appendFilterCondition(originalSql, pageable);
    if ((null != isAdmin && !isAdmin) || !employeeListSearchCondition.isSearched()) {
      resultSql = appendFilterCondition(originalSql + additionalSql, pageable);
    }

    List<?> jobUserList =
        entityManager
            .createNativeQuery(resultSql)
            .setParameter(1, companyId)
            .setParameter(2, employeeListSearchCondition.getKeyword())
            .getResultList();

    jobUserItemList = convertToJobUserList(jobUserList);
    return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
  }

  @Override
  public Page<JobUserListItem> getMyTeamByManager(
      EmployeeListSearchCondition employeeListSearchCondition, User user, Pageable pageable) {

    String countAllTeamMembers =
        "select count(1) from users u where u.deleted_at is null "
            + "and u.manager_user_id = ?1 and u.company_id = ?2";
    BigInteger employeeCount =
        (BigInteger)
            entityManager
                .createNativeQuery(countAllTeamMembers)
                .setParameter(1, user.getId())
                .setParameter(2, user.getCompany().getId())
                .getSingleResult();

    List<JobUserListItem> jobUserItemList = new ArrayList<>();
    if (employeeCount.longValue() == 0) {
      return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
    }

    String originalSql =
        "select u.id as id, u.image_url as iamgeUrl, up.first_name as firstName, "
            + "up.last_name as lastName, d.name as department, j.title as jobTitle "
            + "from users u "
            + "left join  user_personal_information up on u.user_personal_information_id = up.id "
            + "left join jobs_users ju on u.id = ju.user_id "
            + "left join jobs j on ju.job_id = j.id "
            + "left join departments d on j.department_id = d.id "
            + "where u.deleted_at is null "
            + "and ju.deleted_at is null "
            + "and j.deleted_at is null "
            + "and u.manager_user_id = ?1 "
            + "and u.company_id = ?2 "
            + "and (up.first_name like concat('%', ?3, '%') "
            + "or up.last_name like concat('%', ?3, '%') "
            + "or d.name like concat('%', ?3, '%') or j.title like concat('%', ?3, '%')) ";

    String resultSql = appendFilterCondition(originalSql, pageable);

    List<?> jobUserList =
        entityManager
            .createNativeQuery(resultSql)
            .setParameter(1, user.getId())
            .setParameter(2, user.getCompany().getId())
            .setParameter(3, employeeListSearchCondition.getKeyword())
            .getResultList();

    jobUserItemList = convertToJobUserList(jobUserList);
    return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
  }

  @Override
  public List<OrgChartDto> findOrgChartItemByManagerId(Long managerId, Long companyId) {

    StringBuilder findAllOrgChartByCondition =
        new StringBuilder(
            "select u.id as id, up.first_name as firstName, up.last_name as lastName,"
                + "u.image_url as imageUrl, j.title as jobTitle,"
                + "a.city as city, province.name as state, "
                + "d.name as department, u.manager_user_id as managerId "
                + "from users u "
                + "left join user_personal_information up "
                + "on u.user_personal_information_id = up.id "
                + "left join jobs_users jobUser on u.id = jobUser.user_id "
                + "left join jobs j on jobUser.job_id = j.id "
                + "left join departments d on j.department_id=d.id "
                + "left join offices o on jobUser.office_id = o.id "
                + "left join office_addresses a on o.office_address_id = a.id "
                + "left join states_provinces province on a.state_province_id = province.id "
                + "where u.deleted_at is null and u.company_id = ?2");

    if (managerId == null) {
      findAllOrgChartByCondition.append(" and u.manager_user_id is null");
    } else {
      findAllOrgChartByCondition.append(" and u.manager_user_id = ?1");
    }
    findAllOrgChartByCondition.append(" order by u.created_at asc");

    Query findAllOrgChartByConditionQuery =
        entityManager.createNativeQuery(findAllOrgChartByCondition.toString());

    if (managerId != null) {
      findAllOrgChartByConditionQuery.setParameter(1, managerId);
    }
    findAllOrgChartByConditionQuery.setParameter(2, companyId);

    List<?> orgChartItemList = findAllOrgChartByConditionQuery.getResultList();
    List<OrgChartDto> orgChartDtoList = new ArrayList<>();
    orgChartItemList.forEach(
        orgChartItem -> {
          if (orgChartItem instanceof Object[]) {
            Object[] orgChartItemArray = (Object[]) orgChartItem;
            OrgChartDto orgChartDto = new OrgChartDto();
            orgChartDto.setId(((BigInteger) orgChartItemArray[0]).longValue());
            orgChartDto.setFirstName((String) orgChartItemArray[1]);
            orgChartDto.setLastName((String) orgChartItemArray[2]);
            orgChartDto.setImageUrl((String) orgChartItemArray[3]);
            orgChartDto.setJobTitle((String) orgChartItemArray[4]);
            orgChartDto.setCity((String) orgChartItemArray[5]);
            orgChartDto.setState((String) orgChartItemArray[6]);
            orgChartDto.setDepartment((String) orgChartItemArray[7]);
            if (orgChartItemArray[8] != null) {
              orgChartDto.setManagerId(((BigInteger) orgChartItemArray[8]).longValue());
            }
            orgChartDtoList.add(orgChartDto);
          }
        });
    return orgChartDtoList;
  }

  @Override
  public OrgChartDto findOrgChartItemByUserId(Long id, Long companyId) {

    String findAllOrgChartByCondition =
        "select u.id as id, up.first_name as firstName, up.last_name as lastName,"
            + "u.image_url as imageUrl, j.title as jobTitle,"
            + "a.city as city, province.name as state, d.name as department, "
            + "u.manager_user_id as managerId "
            + "from users u "
            + "left join user_personal_information up on u.user_personal_information_id = up.id "
            + "left join jobs_users jobUser on u.id = jobUser.user_id "
            + "left join jobs j on jobUser.job_id = j.id "
            + "left join departments d on j.department_id=d.id "
            + "left join offices o on jobUser.office_id = o.id "
            + "left join office_addresses a on o.office_address_id = a.id "
            + "left join states_provinces province on a.state_province_id = province.id "
            + "where u.id = ?1 and u.company_id = ?2 and u.deleted_at is null "
            + "order by u.created_at asc";
    Query findAllOrgChartByConditionQuery =
        entityManager
            .createNativeQuery(findAllOrgChartByCondition)
            .setParameter(1, id)
            .setParameter(2, companyId);

    Object orgChartItem = findAllOrgChartByConditionQuery.getSingleResult();
    if (orgChartItem instanceof Object[]) {
      Object[] orgChartItemArray = (Object[]) orgChartItem;
      OrgChartDto orgChartDto = new OrgChartDto();
      orgChartDto.setId(((BigInteger) orgChartItemArray[0]).longValue());
      orgChartDto.setFirstName((String) orgChartItemArray[1]);
      orgChartDto.setLastName((String) orgChartItemArray[2]);
      orgChartDto.setImageUrl((String) orgChartItemArray[3]);
      orgChartDto.setJobTitle((String) orgChartItemArray[4]);
      orgChartDto.setCity((String) orgChartItemArray[5]);
      orgChartDto.setState((String) orgChartItemArray[6]);
      orgChartDto.setDepartment((String) orgChartItemArray[7]);
      if (orgChartItemArray[8] != null) {
        orgChartDto.setManagerId(((BigInteger) orgChartItemArray[8]).longValue());
      }
      return orgChartDto;
    }
    return null;
  }

  private String appendFilterCondition(String originalSql, Pageable pageable) {
    StringBuilder resultSql = new StringBuilder(originalSql);
    resultSql.append("order by ");
    StringBuilder finalSql = resultSql;
    pageable
        .getSort()
        .forEach(
            order ->
                finalSql
                    .append(order.getProperty())
                    .append(" ")
                    .append(order.getDirection())
                    .append(","));

    int commaIndex = resultSql.lastIndexOf(",");
    resultSql = resultSql.replace(commaIndex, resultSql.length(), " ");
    resultSql
        .append("limit ")
        .append(pageable.getPageNumber() * pageable.getPageSize())
        .append(",")
        .append(pageable.getPageSize());
    return resultSql.toString();
  }

  private List<JobUserListItem> convertToJobUserList(List<?> jobUserList) {
    List<JobUserListItem> jobUserItemList = new ArrayList<>();
    jobUserList.forEach(
        jobUser -> {
          if (jobUser instanceof Object[]) {
            Object[] jobUserItem = (Object[]) jobUser;
            JobUserListItem jobUserListItem = new JobUserListItem();
            jobUserListItem.setId(((BigInteger) jobUserItem[0]).longValue());
            jobUserListItem.setImageUrl((String) jobUserItem[1]);
            jobUserListItem.setFirstName((String) jobUserItem[2]);
            jobUserListItem.setLastName((String) jobUserItem[3]);
            jobUserListItem.setDepartment((String) jobUserItem[4]);
            jobUserListItem.setJobTitle((String) jobUserItem[5]);
            jobUserItemList.add(jobUserListItem);
          }
        });
    return jobUserItemList;
  }
}
