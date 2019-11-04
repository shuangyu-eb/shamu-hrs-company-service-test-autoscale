package shamu.company.user.repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.event.UserRoleUpdatedEvent;
import shamu.company.utils.Auth0Util;
import shamu.company.utils.TupleUtil;

@Repository
public class UserCustomRepositoryImpl implements UserCustomRepository {
  static final String ACTIVE_USER_QUERY = "and (u.deactivated_at is null "
      + "or (u.deactivated_at is not null "
      + "and u.deactivated_at > current_timestamp)) ";


  private final EntityManager entityManager;

  private final Auth0Util auth0Util;

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public UserCustomRepositoryImpl(final EntityManager entityManager,
      final Auth0Util auth0Util, final ApplicationEventPublisher applicationEventPublisher) {
    this.entityManager = entityManager;
    this.auth0Util = auth0Util;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public Page<JobUserListItem> getAllByCondition(
      final EmployeeListSearchCondition employeeListSearchCondition, final Long companyId,
      final Pageable pageable, final Role role) {

    String countAllEmployees = "";
    if (!employeeListSearchCondition.getIncludeDeactivated()) {
      countAllEmployees =
          "select count(1) from users u where u.company_id = ?1 "
              + ACTIVE_USER_QUERY;
    } else {
      countAllEmployees =
          "select count(1) from users u where u.company_id = ?1";
    }
    final BigInteger employeeCount =
        (BigInteger)
            entityManager
                .createNativeQuery(countAllEmployees)
                .setParameter(1, companyId)
                .getSingleResult();

    if (employeeCount.longValue() == 0) {
      return new PageImpl<>(Collections.emptyList(), pageable, employeeCount.longValue());
    }

    final String originalSql =
        "select u.id as id, u.image_url as imageUrl, up.first_name as firstName, "
            + "up.last_name as lastName, d.name as department, j.title as jobTitle, "
            + "ur.name as roleName "
            + "from users u "
            + "left join  user_personal_information up on u.user_personal_information_id = up.id "
            + "left join jobs_users ju on u.id = ju.user_id "
            + "left join jobs j on ju.job_id = j.id "
            + "left join departments d on j.department_id = d.id "
            + "left join user_roles ur on u.user_role_id = ur.id "
            + "where u.company_id = ?1 "
            + "and (up.first_name like concat('%', ?2, '%') "
            + "or up.last_name like concat('%', ?2, '%') "
            + "or d.name like concat('%', ?2, '%') or j.title like concat('%', ?2, '%')) ";
    final String additionalSql = ACTIVE_USER_QUERY;

    String resultSql = appendFilterCondition(originalSql, pageable);

    if (!employeeListSearchCondition.getIncludeDeactivated()) {
      resultSql = appendFilterCondition(originalSql + additionalSql, pageable);
    }

    final List<?> jobUserList =
        entityManager
            .createNativeQuery(resultSql, Tuple.class)
            .setParameter(1, companyId)
            .setParameter(2, employeeListSearchCondition.getKeyword().trim())
            .getResultList();

    final List<JobUserListItem> jobUserItemList = jobUserList.stream()
        .map(jobUser -> TupleUtil.convertTo((Tuple) jobUser, JobUserListItem.class))
        .collect(Collectors.toList());

    return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
  }

  @Override
  public Page<JobUserListItem> getMyTeamByManager(
      final EmployeeListSearchCondition employeeListSearchCondition, final User user,
      final Pageable pageable) {

    final String roleName = user.getUserRole().getName();
    final boolean isEmployee = StringUtils.equals(Role.EMPLOYEE.getValue(), roleName);

    String userCondition = "u.manager_user_id=?1 ";
    if (isEmployee) {
      userCondition = "(u.id=?1 or " + userCondition + " ) and u.id!=?4 ";
    }
    final String queryColumns =
        "select u.id as id, u.image_url as imageUrl, up.first_name as firstName, "
            + "up.last_name as lastName, d.name as department, j.title as jobTitle, "
            + "ur.name as roleName ";

    final String queryCondition = "from users u "
        + "left join  user_personal_information up on u.user_personal_information_id = up.id "
        + "left join jobs_users ju on u.id = ju.user_id "
        + "left join jobs j on ju.job_id = j.id "
        + "left join departments d on j.department_id = d.id "
        + "left join user_roles ur on u.user_role_id = ur.id "
        + "where "
        + userCondition
        + ACTIVE_USER_QUERY
        + " and u.company_id = ?2 "
        + "and (up.first_name like concat('%', ?3, '%') "
        + "or up.last_name like concat('%', ?3, '%') "
        + "or d.name like concat('%', ?3, '%') or j.title like concat('%', ?3, '%')) ";

    final String countAllTeamMembers = "select count(u.id) as num " + queryCondition;
    final Query queryCount = getQuery(employeeListSearchCondition, user, isEmployee,
        countAllTeamMembers);

    final Tuple employeeTuple = (Tuple) queryCount.getSingleResult();
    final BigInteger employeeCount = (BigInteger) employeeTuple.get("num");

    List<JobUserListItem> jobUserItemList = new ArrayList<>();
    if (employeeCount.longValue() == 0) {
      return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
    }

    final String resultSql = appendFilterCondition(queryColumns + queryCondition, pageable);

    final Query query = getQuery(employeeListSearchCondition, user, isEmployee, resultSql);
    final List<?> jobUserList = query.getResultList();

    jobUserItemList = jobUserList.stream()
        .map(jobUser -> TupleUtil.convertTo((Tuple) jobUser, JobUserListItem.class))
        .collect(Collectors.toList());

    return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
  }

  private Query getQuery(final EmployeeListSearchCondition employeeListSearchCondition,
      final User user, final boolean isEmployee, final String resultSql) {
    final User manager = isEmployee ? user.getManagerUser() : user;
    final Query query = entityManager
        .createNativeQuery(resultSql, Tuple.class)
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
                + "where u.company_id = ?2 "
                + ACTIVE_USER_QUERY);

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
            + "where u.id = ?1 and u.company_id = ?2 "
            + ACTIVE_USER_QUERY
            + "order by u.created_at asc";
    final Query findAllOrgChartByConditionQuery =
        entityManager
            .createNativeQuery(findAllOrgChartByCondition, Tuple.class)
            .setParameter(1, id)
            .setParameter(2, companyId);

    final Object orgChartItem = findAllOrgChartByConditionQuery.getSingleResult();
    return TupleUtil.convertTo((Tuple) orgChartItem, OrgChartDto.class);
  }

  @Override
  public User saveUser(final User user) {
    if (user.getId() != null) {
      final User existingUser = entityManager.find(User.class, user.getId());
      if (existingUser != null && user.getUserRole() != existingUser.getUserRole()) {
        auth0Util.updateRoleWithUserId(existingUser.getUserId(), user.getUserRole().getName());
        applicationEventPublisher.publishEvent(new UserRoleUpdatedEvent(existingUser.getUserId(),
            existingUser.getUserRole()));
      }
    }

    final User returnUser;
    if (user.getId() != null) {
      returnUser = entityManager.merge(user);
    } else {
      entityManager.persist(user);
      returnUser = user;
    }

    return returnUser;
  }

  @Override
  public List<User> saveAllUsers(final Iterable<User> users) {
    final Iterator<User> userIterator = users.iterator();
    final List<User> returnUserList = new ArrayList<>();
    while (userIterator.hasNext()) {
      returnUserList.add(saveUser(userIterator.next()));
    }
    return returnUserList;
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

  @SuppressWarnings("unused")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
  public void restoreUserRole(final UserRoleUpdatedEvent userRoleUpdatedEvent) {
    auth0Util.updateAuthRole(userRoleUpdatedEvent.getUserId(),
        userRoleUpdatedEvent.getUserRole().getName());
  }
}
