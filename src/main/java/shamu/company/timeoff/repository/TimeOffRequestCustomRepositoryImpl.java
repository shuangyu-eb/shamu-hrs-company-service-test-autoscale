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
}
