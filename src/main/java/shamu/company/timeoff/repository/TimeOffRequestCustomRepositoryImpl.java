package shamu.company.timeoff.repository;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.stereotype.Repository;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.utils.UuidUtil;

@Repository
public class TimeOffRequestCustomRepositoryImpl implements TimeOffRequestCustomRepository {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<String> getFilteredReviewedTimeOffRequestsIds(
      final String userId, final Long startTime, final Long endTime) {
    String query =
        "SELECT hex(request.id) FROM time_off_requests request "
            + "left join time_off_request_approval_statuses rs "
            + "on request.time_off_request_approval_status_id = rs.id "
            + "WHERE "
            + " (rs.name = ?1 or rs.name = ?2 ) "
            + "AND requester_user_id = unhex(?3) ";
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    if (startTime != null) {
      query += "AND DATE_FORMAT(approved_date, \'%Y%m%d\')  >= ?4 ";
    }
    final Calendar calendar = Calendar.getInstance();
    final Date endDate = endTime == null ? new Date() : new Date(endTime);
    calendar.setTime(endDate);
    query += "AND DATE_FORMAT(approved_date, \'%Y%m%d\') <= ?5 ";

    final Query queryResult = entityManager.createNativeQuery(query);
    queryResult.setParameter(1, TimeOffApprovalStatus.APPROVED.name());
    queryResult.setParameter(2, TimeOffApprovalStatus.DENIED.name());
    queryResult.setParameter(3, userId);
    if (startTime != null) {
      queryResult.setParameter(4, simpleDateFormat.format(startTime));
    }
    queryResult.setParameter(5, simpleDateFormat.format(calendar.getTime()));
    return (List<String>) queryResult.getResultList();
  }

  @Override
  public List<TimeOffRequest> findByTimeOffPolicyUserAndStatus(
      final String userId, final String policyId, final TimeOffApprovalStatus status,
      final Timestamp currentTime, final TimeOffRequestDate.Operator operator) {
    final StringBuilder queryTimeOffRequestDate =
            new StringBuilder(
                    "select tor.id as tid, tord.id as did, tord.hours, tord.date "
                            + "from time_off_requests tor "
                            + "join time_off_request_dates tord "
                            + " on tor.id = tord.time_off_request_id "
                            + "join time_off_request_approval_statuses rs "
                            + " on tor.time_off_request_approval_status_id = rs.id "
                            + "where tor.requester_user_id = unhex(?1) "
                            + "and tor.time_off_policy_id = unhex(?2) "
                            + "and rs.name = ?3 "
                            + "group by tord.id ");
    if (null != currentTime && operator.equals(TimeOffRequestDate.Operator.LESS_THAN)) {
      queryTimeOffRequestDate.append(" having min(tord.date) <= ?4 ");
    } else if (null != currentTime && operator.equals(TimeOffRequestDate.Operator.MORE_THAN)) {
      queryTimeOffRequestDate.append(" having min(tord.date) > ?4 ");
    }

    final Query queryTimeOffRequestDateResult =
            entityManager.createNativeQuery(queryTimeOffRequestDate.toString());
    queryTimeOffRequestDateResult.setParameter(1, userId);
    queryTimeOffRequestDateResult.setParameter(2, policyId);
    queryTimeOffRequestDateResult.setParameter(3, status.name());

    if (null != currentTime) {
      queryTimeOffRequestDateResult.setParameter(4, currentTime);
    }

    final String queryTimeOffRequest =
        "select tor.* from time_off_requests tor "
            + " left join time_off_request_approval_statuses rs "
            + " on tor.time_off_request_approval_status_id = rs.id "
            + "where tor.requester_user_id = unhex(?1) "
            + "and tor.time_off_policy_id = unhex(?2) "
            + "and rs.name = ?3 ";
    final Query queryTimeOffRequestResult =
            entityManager.createNativeQuery(queryTimeOffRequest);

    queryTimeOffRequestResult.setParameter(1, userId);
    queryTimeOffRequestResult.setParameter(2, policyId);
    queryTimeOffRequestResult.setParameter(3, status.name());
    final List<?> timeOffRequestItemList = queryTimeOffRequestResult.getResultList();

    final List<?> timeOffItemList = queryTimeOffRequestDateResult.getResultList();
    final List<TimeOffRequest> timeOffRequestList = new ArrayList<>();
    timeOffRequestItemList.forEach(
        timeOffRequestItem -> {
          if (timeOffRequestItem instanceof Object[]) {
            final Object[] timeOffRequestItemArray = (Object[]) timeOffRequestItem;
            final TimeOffRequest timeOffRequest = new TimeOffRequest();
            final Set<TimeOffRequestDate> timeOffRequestDateList = new HashSet<>();
            timeOffItemList.forEach(
                timeOffItem -> {
                  if (timeOffItem instanceof Object[]
                          && UuidUtil.toHexString((byte[]) timeOffRequestItemArray[0])
                          .equals(UuidUtil.toHexString((byte[]) ((Object[]) timeOffItem)[0]))) {
                    final Object[] timeOffItemArray = (Object[]) timeOffItem;
                    final TimeOffRequestDate timeOffRequestDate = new TimeOffRequestDate();
                    timeOffRequestDate.setId(
                            UuidUtil.toHexString((byte[]) timeOffItemArray[1]));
                    timeOffRequestDate.setHours(((Integer) timeOffItemArray[2]));
                    timeOffRequestDate.setDate((Timestamp) timeOffItemArray[3]);
                    timeOffRequestDateList.add(timeOffRequestDate);
                  }
                }
            );
            timeOffRequest.setId(UuidUtil.toHexString((byte[]) timeOffRequestItemArray[0]));
            timeOffRequest.setTimeOffRequestDates(timeOffRequestDateList);
            if (!timeOffRequestDateList.isEmpty()) {
              timeOffRequestList.add(timeOffRequest);
            }
          }
        }
    );

    return timeOffRequestList;
  }
}
