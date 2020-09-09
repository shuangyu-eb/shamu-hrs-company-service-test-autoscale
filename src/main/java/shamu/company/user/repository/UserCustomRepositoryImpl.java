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
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.event.UserRoleUpdatedEvent;
import shamu.company.utils.TupleUtil;

@Repository
public class UserCustomRepositoryImpl implements UserCustomRepository {
  static final String ACTIVE_USER_QUERY =
      " (u.deactivated_at is null "
          + "or (u.deactivated_at is not null "
          + "and u.deactivated_at > current_timestamp)) ";

  static final String AND_ACTIVE_USER_QUERY = " and " + ACTIVE_USER_QUERY;

  static final String FROM_SQL =
      "from users u "
          + "left join  user_personal_information up on u.user_personal_information_id = up.id "
          + "left join jobs_users ju on u.id = ju.user_id "
          + "left join jobs j on ju.job_id = j.id "
          + "left join departments d on ju.department_id = d.id "
          + "left join user_roles ur on u.user_role_id = ur.id ";

  private final EntityManager entityManager;

  private final Auth0Helper auth0Helper;

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public UserCustomRepositoryImpl(
      final EntityManager entityManager,
      final Auth0Helper auth0Helper,
      final ApplicationEventPublisher applicationEventPublisher) {
    this.entityManager = entityManager;
    this.auth0Helper = auth0Helper;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public Page<JobUserListItem> getAllByCondition(
      final EmployeeListSearchCondition employeeListSearchCondition, final Pageable pageable) {

    final String conditionSql =
        FROM_SQL
            + "where (concat(up.first_name, ' ', up.last_name) like concat('%', ?1, '%') "
            + "or concat(up.preferred_name, ' ', up.last_name) like concat('%', ?1, '%') "
            + "or d.name like concat('%', ?1, '%') or j.title like concat('%', ?1, '%')) ";

    return queryJobUserListItem(conditionSql, employeeListSearchCondition, pageable);
  }

  private Page<JobUserListItem> queryJobUserListItem(
      final String conditionSql,
      final EmployeeListSearchCondition employeeListSearchCondition,
      final Pageable pageable) {
    String countAllEmployees = "select count(1) " + conditionSql;
    if (!employeeListSearchCondition.getIncludeDeactivated()) {
      countAllEmployees += AND_ACTIVE_USER_QUERY;
    }
    final BigInteger employeeCount =
        (BigInteger)
            entityManager
                .createNativeQuery(countAllEmployees)
                .setParameter(1, employeeListSearchCondition.getKeyword().trim())
                .getSingleResult();

    if (employeeCount.longValue() == 0) {
      return new PageImpl<>(Collections.emptyList(), pageable, employeeCount.longValue());
    }

    final String originalSql =
        "select u.id as id, u.image_url as imageUrl, up.first_name as firstName, "
            + "up.last_name as lastName, d.name as department, j.title as jobTitle, "
            + "up.preferred_name as preferredName, ur.name as roleName "
            + conditionSql;

    String resultSql = appendFilterCondition(originalSql, pageable);

    if (!employeeListSearchCondition.getIncludeDeactivated()) {
      resultSql = appendFilterCondition(originalSql + AND_ACTIVE_USER_QUERY, pageable);
    }

    final List<?> jobUserList =
        entityManager
            .createNativeQuery(resultSql, Tuple.class)
            .setParameter(1, employeeListSearchCondition.getKeyword().trim())
            .getResultList();

    final List<JobUserListItem> jobUserItemList =
        jobUserList.stream()
            .map(jobUser -> TupleUtil.convertTo((Tuple) jobUser, JobUserListItem.class))
            .collect(Collectors.toList());

    return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
  }

  @Override
  public Page<JobUserListItem> getMyTeamByManager(
      final EmployeeListSearchCondition employeeListSearchCondition,
      final User user,
      final Pageable pageable) {

    final String roleName = user.getUserRole().getName();
    final boolean isEmployee = StringUtils.equals(Role.EMPLOYEE.getValue(), roleName);
    final boolean hasManager = user.getManagerUser() != null;
    String userCondition = "u.manager_user_id= unhex(?3)";
    if (hasManager) {
      userCondition = userCondition + " or u.id= unhex(?1)";
      if (isEmployee) {
        userCondition = "u.id=unhex(?1) or (u.manager_user_id= unhex(?1) and u.id!=unhex(?3))";
      }
    }
    final String queryColumns =
        "select u.id as id, u.image_url as imageUrl, up.first_name as firstName, "
            + "up.last_name as lastName, up.preferred_name as preferredName, "
            + "d.name as department, j.title as jobTitle, ur.name as roleName ";

    final String queryCondition =
        FROM_SQL
            + "where "
            + userCondition
            + AND_ACTIVE_USER_QUERY
            + " and (up.first_name like concat('%', ?2, '%') "
            + "or up.last_name like concat('%', ?2, '%') "
            + "or d.name like concat('%', ?2, '%') or j.title like concat('%', ?2, '%')) ";

    final String countAllTeamMembers = "select count(u.id) as num " + queryCondition;
    final Query queryCount =
        getQuery(employeeListSearchCondition, user, hasManager, countAllTeamMembers);

    final Tuple employeeTuple = (Tuple) queryCount.getSingleResult();
    final BigInteger employeeCount = (BigInteger) employeeTuple.get("num");

    List<JobUserListItem> jobUserItemList = new ArrayList<>();
    if (employeeCount.longValue() == 0) {
      return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
    }

    final String resultSql = appendFilterCondition(queryColumns + queryCondition, pageable);

    final Query query = getQuery(employeeListSearchCondition, user, hasManager, resultSql);
    final List<?> jobUserList = query.getResultList();

    jobUserItemList =
        jobUserList.stream()
            .map(jobUser -> TupleUtil.convertTo((Tuple) jobUser, JobUserListItem.class))
            .collect(Collectors.toList());

    return new PageImpl<>(jobUserItemList, pageable, employeeCount.longValue());
  }

  private Query getQuery(
      final EmployeeListSearchCondition employeeListSearchCondition,
      final User user,
      final boolean hasManager,
      final String resultSql) {
    final User manager = hasManager ? user.getManagerUser() : user;
    final Query query =
        entityManager
            .createNativeQuery(resultSql, Tuple.class)
            .setParameter(3, user.getId())
            .setParameter(2, employeeListSearchCondition.getKeyword());
    if (hasManager) {
      query.setParameter(1, manager.getId());
    }

    return query;
  }

  @Override
  public List<OrgChartDto> findOrgChartItemByManagerId(final String managerId) {

    final StringBuilder findAllOrgChartByCondition =
        new StringBuilder(
            "select u.id as id, up.first_name as firstName, up.last_name as lastName,"
                + "up.preferred_name as preferredName, u.image_url as imageUrl,"
                + " j.title as jobTitle,"
                + "a.city as city, province.name as state, "
                + "d.name as department, u.manager_user_id as managerId "
                + "from users u "
                + "left join user_personal_information up "
                + "on u.user_personal_information_id = up.id "
                + "left join jobs_users jobUser on u.id = jobUser.user_id "
                + "left join jobs j on jobUser.job_id = j.id "
                + "left join departments d on jobUser.department_id=d.id "
                + "left join offices o on jobUser.office_id = o.id "
                + "left join office_addresses a on o.office_address_id = a.id "
                + "left join states_provinces province on a.state_province_id = province.id "
                + "where "
                + ACTIVE_USER_QUERY);

    if (managerId == null) {
      findAllOrgChartByCondition.append(" and u.manager_user_id is null");
    } else {
      findAllOrgChartByCondition.append(" and u.manager_user_id = unhex(?1)");
    }
    findAllOrgChartByCondition.append(" order by u.created_at asc");

    final Query findAllOrgChartByConditionQuery =
        entityManager.createNativeQuery(findAllOrgChartByCondition.toString(), Tuple.class);

    if (!StringUtils.isEmpty(managerId)) {
      findAllOrgChartByConditionQuery.setParameter(1, managerId);
    }

    final List<?> orgChartItemList = findAllOrgChartByConditionQuery.getResultList();
    return orgChartItemList.stream()
        .map(orgChart -> TupleUtil.convertTo((Tuple) orgChart, OrgChartDto.class))
        .collect(Collectors.toList());
  }

  @Override
  public OrgChartDto findOrgChartItemByUserId(final String id) {

    final String findAllOrgChartByCondition =
        "select u.id as id, up.first_name as firstName, up.last_name as lastName,"
            + "up.preferred_name as preferredName, u.image_url as imageUrl, j.title as jobTitle,"
            + "a.city as city, province.name as state, d.name as department, "
            + "u.manager_user_id as managerId "
            + "from users u "
            + "left join user_personal_information up on u.user_personal_information_id = up.id "
            + "left join jobs_users jobUser on u.id = jobUser.user_id "
            + "left join jobs j on jobUser.job_id = j.id "
            + "left join departments d on jobUser.department_id=d.id "
            + "left join offices o on jobUser.office_id = o.id "
            + "left join office_addresses a on o.office_address_id = a.id "
            + "left join states_provinces province on a.state_province_id = province.id "
            + "where u.id = unhex(?1) and "
            + ACTIVE_USER_QUERY
            + "order by u.created_at asc";
    final Query findAllOrgChartByConditionQuery =
        entityManager
            .createNativeQuery(findAllOrgChartByCondition, Tuple.class)
            .setParameter(1, id);

    final Object orgChartItem = findAllOrgChartByConditionQuery.getSingleResult();
    return TupleUtil.convertTo((Tuple) orgChartItem, OrgChartDto.class);
  }

  @Override
  public User saveUser(final User user) {
    entityManager.detach(user);
    final User existingUser = entityManager.find(User.class, user.getId());

    if (existingUser != null && existingUser.getRole() != user.getRole()) {
      auth0Helper.updateRole(existingUser, user.getUserRole().getName());
      applicationEventPublisher.publishEvent(
          new UserRoleUpdatedEvent(existingUser.getId(), existingUser.getUserRole()));
    }

    final User returnUser;
    if (user.getId() != null && existingUser != null) {
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

  @Override
  public Page<JobUserListItem> getAllByName(
      final EmployeeListSearchCondition employeeListSearchCondition, final Pageable pageable) {

    final String conditionSql =
        FROM_SQL
            + "where (concat(up.first_name, ' ', up.last_name) like concat('%', ?1, '%') "
            + "or concat(up.preferred_name, ' ', up.last_name) like concat('%', ?1, '%'))";

    return queryJobUserListItem(conditionSql, employeeListSearchCondition, pageable);
  }

  private String appendFilterCondition(final String originalSql, final Pageable pageable) {
    final StringBuilder resultSql = new StringBuilder(originalSql);
    resultSql.append("order by ");
    pageable
        .getSort()
        .forEach(
            order ->
                resultSql
                    .append(order.getProperty())
                    .append(" ")
                    .append(order.getDirection())
                    .append(","));

    final int commaIndex = resultSql.lastIndexOf(",");
    resultSql.replace(commaIndex, resultSql.length(), " ");
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
    auth0Helper.updateAuthRole(
        userRoleUpdatedEvent.getUserId(), userRoleUpdatedEvent.getUserRole().getName());
  }
}
