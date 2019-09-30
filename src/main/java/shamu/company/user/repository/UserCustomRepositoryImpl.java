package shamu.company.user.repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.utils.Auth0Util;
import shamu.company.utils.DateUtil;

@Repository
public class UserCustomRepositoryImpl implements UserCustomRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private Auth0Util auth0Util;

  @Override
  public Page<JobUserListItem> getAllByCondition(
      final EmployeeListSearchCondition employeeListSearchCondition, final Long companyId,
      final Pageable pageable, final Role role) {

    String countAllEmployees = "";
    final boolean isAdmin = Role.ADMIN.equals(role);
    if (!isAdmin || !employeeListSearchCondition.isSearched()) {
      countAllEmployees =
          "select count(1) from users u where u.deleted_at is null "
              + " and u.deactivated = false and u.company_id = ?1 ";
    } else {
      countAllEmployees =
          "select count(1) from users u where u.deleted_at is null "
              + "and u.company_id = ?1";
    }
    final BigInteger employeeCount =
        (BigInteger)
            entityManager
                .createNativeQuery(countAllEmployees)
                .setParameter(1, companyId)
                .getSingleResult();

    List<JobUserListItem> jobUserItemList = new ArrayList<>();
    if (employeeCount.longValue() == 0) {
      return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
    }

    final String originalSql =
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
    final String additionalSql = " and u.deactivated = false ";

    String resultSql = appendFilterCondition(originalSql, pageable);
    if (!isAdmin || !employeeListSearchCondition.isSearched()) {
      resultSql = appendFilterCondition(originalSql + additionalSql, pageable);
    }

    final List<?> jobUserList =
        entityManager
            .createNativeQuery(resultSql)
            .setParameter(1, companyId)
            .setParameter(2, employeeListSearchCondition.getKeyword().trim())
            .getResultList();

    jobUserItemList = convertToJobUserList(jobUserList);
    return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
  }

  @Override
  public Page<JobUserListItem> getMyTeamByManager(
      final EmployeeListSearchCondition employeeListSearchCondition, final User user,
      final Pageable pageable) {

    final Role role = auth0Util.getUserRole(user.getUserId());
    final boolean isEmployee = Role.EMPLOYEE == role;

    String userCondition = "u.manager_user_id=?1 ";
    if (isEmployee) {
      userCondition = "(u.id=?1 or " + userCondition + " ) and u.id!=?4 ";
    }
    final String queryColumns =
        "select u.id as id, u.image_url as iamgeUrl, up.first_name as firstName, "
            + "up.last_name as lastName, d.name as department, j.title as jobTitle ";

    final String queryCondition = "from users u "
        + "left join  user_personal_information up on u.user_personal_information_id = up.id "
        + "left join jobs_users ju on u.id = ju.user_id "
        + "left join jobs j on ju.job_id = j.id "
        + "left join departments d on j.department_id = d.id "
        + "where u.deleted_at is null "
        + "and u.deactivated = false "
        + "and ju.deleted_at is null "
        + "and j.deleted_at is null "
        + "and " + userCondition
        + " and u.company_id = ?2 "
        + "and (up.first_name like concat('%', ?3, '%') "
        + "or up.last_name like concat('%', ?3, '%') "
        + "or d.name like concat('%', ?3, '%') or j.title like concat('%', ?3, '%')) ";

    final String countAllTeamMembers = "select count(u.id) " + queryCondition;
    final Query queryCount = getQuery(employeeListSearchCondition, user, isEmployee,
        countAllTeamMembers);

    final BigInteger employeeCount = (BigInteger) queryCount.getSingleResult();

    List<JobUserListItem> jobUserItemList = new ArrayList<>();
    if (employeeCount.longValue() == 0) {
      return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
    }

    final String resultSql = appendFilterCondition(queryColumns + queryCondition, pageable);

    final Query query = getQuery(employeeListSearchCondition, user, isEmployee, resultSql);
    final List<?> jobUserList = query.getResultList();

    jobUserItemList = convertToJobUserList(jobUserList);
    return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
  }

  private Query getQuery(final EmployeeListSearchCondition employeeListSearchCondition,
      final User user, final boolean isEmployee, final String resultSql) {
    final User manager = isEmployee ? user.getManagerUser() : user;
    final Query query = entityManager
        .createNativeQuery(resultSql)
        .setParameter(1, manager.getId())
        .setParameter(2, manager.getCompany().getId())
        .setParameter(3, employeeListSearchCondition.getKeyword());
    if (isEmployee) {
      query.setParameter(4, user.getId());
    }
    return query;
  }

  @Override
  public List<OrgChartDto> findOrgChartItemByManagerId(final Long managerId, final Long companyId) {

    final StringBuilder findAllOrgChartByCondition =
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
                + "where u.deleted_at is null and u.company_id = ?2 "
                + "and (u.deactivated = false "
                + "or (u.deactivated = true and u.deactivated_at > ?3 )) ");

    if (managerId == null) {
      findAllOrgChartByCondition.append(" and u.manager_user_id is null");
    } else {
      findAllOrgChartByCondition.append(" and u.manager_user_id = ?1");
    }
    findAllOrgChartByCondition.append(" order by u.created_at asc");

    final Query findAllOrgChartByConditionQuery =
        entityManager.createNativeQuery(findAllOrgChartByCondition.toString());

    if (managerId != null) {
      findAllOrgChartByConditionQuery.setParameter(1, managerId);
    }
    findAllOrgChartByConditionQuery.setParameter(2, companyId);
    findAllOrgChartByConditionQuery.setParameter(3, DateUtil.getLocalUtcTime());

    final List<?> orgChartItemList = findAllOrgChartByConditionQuery.getResultList();
    final List<OrgChartDto> orgChartDtoList = new ArrayList<>();
    orgChartItemList.forEach(
        orgChartItem -> {
          if (orgChartItem instanceof Object[]) {
            final Object[] orgChartItemArray = (Object[]) orgChartItem;
            final OrgChartDto orgChartDto = new OrgChartDto();
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
  public OrgChartDto findOrgChartItemByUserId(final Long id, final Long companyId) {

    final String findAllOrgChartByCondition =
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
            + "and (u.deactivated = false "
            + "or (u.deactivated = true and u.deactivated_at > ?3 )) "
            + "order by u.created_at asc";
    final Query findAllOrgChartByConditionQuery =
        entityManager
            .createNativeQuery(findAllOrgChartByCondition)
            .setParameter(1, id)
            .setParameter(2, companyId)
            .setParameter(3, DateUtil.getLocalUtcTime());

    final Object orgChartItem = findAllOrgChartByConditionQuery.getSingleResult();
    if (orgChartItem instanceof Object[]) {
      final Object[] orgChartItemArray = (Object[]) orgChartItem;
      final OrgChartDto orgChartDto = new OrgChartDto();
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

  private String appendFilterCondition(final String originalSql, final Pageable pageable) {
    StringBuilder resultSql = new StringBuilder(originalSql);
    resultSql.append("order by ");
    final StringBuilder finalSql = resultSql;
    pageable
        .getSort()
        .forEach(
            order ->
                finalSql
                    .append(order.getProperty())
                    .append(" ")
                    .append(order.getDirection())
                    .append(","));

    final int commaIndex = resultSql.lastIndexOf(",");
    resultSql = resultSql.replace(commaIndex, resultSql.length(), " ");
    resultSql
        .append("limit ")
        .append(pageable.getPageNumber() * pageable.getPageSize())
        .append(",")
        .append(pageable.getPageSize());
    return resultSql.toString();
  }

  private List<JobUserListItem> convertToJobUserList(final List<?> jobUserList) {
    final List<JobUserListItem> jobUserItemList = new ArrayList<>();
    jobUserList.forEach(
        jobUser -> {
          if (jobUser instanceof Object[]) {
            final Object[] jobUserItem = (Object[]) jobUser;
            final JobUserListItem jobUserListItem = new JobUserListItem();
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
