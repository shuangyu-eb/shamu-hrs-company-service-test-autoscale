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
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestDate;

@Repository
public class TimeOffRequestCustomRepositoryImpl implements TimeOffRequestCustomRepository {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<String> getFilteredReviewedTimeOffRequestsIds(
      String userId, Long startTime, Long endTime) {
    String query =
        "SELECT hex(id) FROM time_off_requests request "
            + "left join time_off_request_approval_statuses rs "
            + "on request.time_off_request_approval_status_id = rs.id "
            + "WHERE "
            + " (rs.name = '" + TimeOffApprovalStatus.APPROVED.name() + "'"
            + "   or rs.name = '" + TimeOffApprovalStatus.DENIED.name() + "')"
            + " AND requester_user_id = unhex("
            + userId + ")";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    if (startTime != null) {
      query +=
          " AND DATE_FORMAT(approved_date, \'%Y%m%d\')  >= \'"
              + simpleDateFormat.format(startTime)
              + "\'";
    }
    Calendar calendar = Calendar.getInstance();
    Date endDate = endTime == null ? new Date() : new Date(endTime);
    calendar.setTime(endDate);
    query +=
        " AND DATE_FORMAT(approved_date, \'%Y%m%d\') <= \'"
            + simpleDateFormat.format(calendar.getTime())
            + "\'";

    return (List<String>) this.entityManager.createNativeQuery(query).getResultList();
  }

  // TODO test id transform
  @Override
  public List<TimeOffRequest> findByTimeOffPolicyUserAndStatus(
          final String userId, final String policyId,
          final TimeOffApprovalStatus status, Timestamp currentTime) {
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
    if (null != currentTime) {
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

    final StringBuilder queryTimeOffRequest =
            new StringBuilder(
                    "select tor.* from time_off_requests tor "
                        + " left join time_off_request_approval_statuses rs "
                        + " on tor.time_off_request_approval_status_id = rs.id "
                        + "where tor.requester_user_id = unhex(?1) "
                        + "and tor.time_off_policy_id = unhex(?2) "
                        + "and rs.name = ?3 ");
    final Query queryTimeOffRequestResult =
            entityManager.createNativeQuery(queryTimeOffRequest.toString());

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
                          && timeOffRequestItemArray[0].equals(((Object[]) timeOffItem)[0])) {
                    final Object[] timeOffItemArray = (Object[]) timeOffItem;
                    final TimeOffRequestDate timeOffRequestDate = new TimeOffRequestDate();
                    timeOffRequestDate.setId((String) timeOffItemArray[1]);
                    timeOffRequestDate.setHours(((Integer) timeOffItemArray[2]));
                    timeOffRequestDate.setDate((Timestamp) timeOffItemArray[3]);
                    timeOffRequestDateList.add(timeOffRequestDate);
                  }
                }
            );
            timeOffRequest.setId((String) timeOffRequestItemArray[0]);
            timeOffRequest.setTimeOffRequestDates(timeOffRequestDateList);
            if (timeOffRequestDateList.size() > 0) {
              timeOffRequestList.add(timeOffRequest);
            }
          }
        }
    );

    return timeOffRequestList;
  }
}
