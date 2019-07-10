package shamu.company.timeoff.repository;

import java.util.List;

public interface TimeOffRequestCustomRepository {

  List<Long> getFilteredReviewedTimeOffRequestsIds(Long userId, Long startTime, Long endTime);
}
