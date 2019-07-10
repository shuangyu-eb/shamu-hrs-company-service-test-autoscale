package shamu.company.timeoff.service;

import java.time.LocalDateTime;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;

public interface TimeOffDetailService {

  TimeOffBreakdownDto getTimeOffBreakdown(Long policyUserId, LocalDateTime endDateTime);
}
