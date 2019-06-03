package shamu.company.timeoff.repository;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class TimeOffRequestCustomRepositoryImpl implements TimeOffRequestCustomRepository {

  @PersistenceContext
  private EntityManager entityManager;

  public List<Long> getTimeOffRequestHistoryIds(
      Long userId, Long startTime, Long endTime) {
    String query =
        "SELECT id FROM time_off_requests WHERE deleted_at IS NULL"
            + " AND time_off_request_approval_status_id = 3"
            + " AND requester_user_id = " + userId;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    if (startTime != null) {
      query +=
          " AND DATE_FORMAT(approved_date, \'%Y%m%d\')  >= \'" + simpleDateFormat.format(startTime)
              + "\'";
    }
    Calendar calendar = Calendar.getInstance();
    Date endDate = endTime == null ? new Date() : new Date(endTime);
    calendar.setTime(endDate);
    query += " AND DATE_FORMAT(approved_date, \'%Y%m%d\') <= \'" + simpleDateFormat
        .format(calendar.getTime()) + "\'";

    List<BigInteger> resultList = entityManager.createNativeQuery(query).getResultList();
    return resultList.stream().map(
        timeOffRequestId -> timeOffRequestId.longValue()
    ).collect(Collectors.toList());
  }
}
