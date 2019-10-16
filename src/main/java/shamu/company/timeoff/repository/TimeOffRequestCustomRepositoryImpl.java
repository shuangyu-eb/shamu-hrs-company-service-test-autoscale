package shamu.company.timeoff.repository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestDate;

@Repository
public class TimeOffRequestCustomRepositoryImpl implements TimeOffRequestCustomRepository {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<Long> getFilteredReviewedTimeOffRequestsIds(
      Long userId, Long startTime, Long endTime) {
    String query =
        "SELECT id FROM time_off_requests WHERE deleted_at IS NULL"
            + " AND time_off_request_approval_status_id IN (3,4)"
            + " AND requester_user_id = "
            + userId;
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

    List<BigInteger> resultList = this.entityManager.createNativeQuery(query).getResultList();
    return resultList.stream()
        .map(timeOffRequestId -> timeOffRequestId.longValue())
        .collect(Collectors.toList());
  }

  @Override
  public List<TimeOffRequest> findByTimeOffPolicyUserAndStatus(
          final Long userId, final Long policyId, final Long statusId, Timestamp currentTime) {
    final StringBuilder queryTimeOffRequestDate =
            new StringBuilder(
                    "select tor.id as tid, tord.id as did, tord.hours, tord.date "
                            + "from time_off_requests tor "
                            + "join time_off_request_dates tord "
                            + "on tor.id = tord.time_off_request_id "
                            + "where tor.deleted_at is null "
                            + "and tor.requester_user_id = ?1 "
                            + "and tor.time_off_policy_id = ?2 "
                            + "and tor.time_off_request_approval_status_id = ?3 "
                            + "group by tord.id ");
    if (null != currentTime) {
      queryTimeOffRequestDate.append(" having min(tord.date > ?4) ");
    }

    final Query queryTimeOffRequestDateResult =
            entityManager.createNativeQuery(queryTimeOffRequestDate.toString());
    queryTimeOffRequestDateResult.setParameter(1, userId);
    queryTimeOffRequestDateResult.setParameter(2, policyId);
    queryTimeOffRequestDateResult.setParameter(3, statusId);

    if (null != currentTime) {
      queryTimeOffRequestDateResult.setParameter(4, currentTime);
    }

    final StringBuilder queryTimeOffRequest =
            new StringBuilder(
                    "select * from time_off_requests tor "
                            + "where tor.deleted_at is null "
                            + "and tor.requester_user_id = ?1 "
                            + "and tor.time_off_policy_id = ?2 "
                            + "and tor.time_off_request_approval_status_id = ?3 ");
    final Query queryTimeOffRequestResult =
            entityManager.createNativeQuery(queryTimeOffRequest.toString());
    queryTimeOffRequestResult.setParameter(1, userId);
    queryTimeOffRequestResult.setParameter(2, policyId);
    queryTimeOffRequestResult.setParameter(3, statusId);
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
                    timeOffRequestDate.setId(((BigInteger) timeOffItemArray[1]).longValue());
                    timeOffRequestDate.setHours(((Integer) timeOffItemArray[2]));
                    timeOffRequestDate.setDate((Timestamp) timeOffItemArray[3]);
                    timeOffRequestDateList.add(timeOffRequestDate);
                  }
                }
            );
            timeOffRequest.setId(((BigInteger) timeOffRequestItemArray[0]).longValue());
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
