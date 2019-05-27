package shamu.company.timeoff.request.entity;

import shamu.company.common.BaseEnumConverter;
import shamu.company.common.ConverterTable;

@ConverterTable("time_off_request_approval_statuses")
public enum TimeOffRequestApprovalStatus {
  NO_ACTION,
  VIEWED,
  APPROVED,
  DENIED;


  public static class Converter extends BaseEnumConverter<TimeOffRequestApprovalStatus> {

  }
}
