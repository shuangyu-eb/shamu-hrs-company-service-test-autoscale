package shamu.company.timeoff.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.repository.TimeOffRequestApprovalStatusRepository;

@Service
public class TimeOffRequestApprovalStatusService {

  private final TimeOffRequestApprovalStatusRepository timeOffRequestApprovalStatusRepository;

  @Autowired
  public TimeOffRequestApprovalStatusService(
      TimeOffRequestApprovalStatusRepository timeOffRequestApprovalStatusRepository) {
    this.timeOffRequestApprovalStatusRepository = timeOffRequestApprovalStatusRepository;
  }

  public TimeOffRequestApprovalStatus findByName(String name) {
    return timeOffRequestApprovalStatusRepository.findByName(name);
  }
}
