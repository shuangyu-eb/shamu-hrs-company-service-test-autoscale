package shamu.company.timeoff.repository;

import java.util.List;

public interface TimeOffRequestCustomRepository {

  List<Long> getTimeOffRequestHistoryIds(Long userId, Long startTime, Long endTime);
}
