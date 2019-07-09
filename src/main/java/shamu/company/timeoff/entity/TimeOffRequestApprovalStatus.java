package shamu.company.timeoff.entity;

import java.util.Arrays;
import java.util.stream.Collectors;
import shamu.company.common.BaseEnumConverter;
import shamu.company.common.ConverterTable;

@ConverterTable("time_off_request_approval_statuses")
public enum TimeOffRequestApprovalStatus {
  NO_ACTION(1L),
  VIEWED(2L),
  APPROVED(3L),
  DENIED(4L);

  private Long value;

  TimeOffRequestApprovalStatus(Long value) {
    this.value = value;
  }

  public void setValue(Long value) {
    this.value = value;
  }

  public Long getValue() {
    return value;
  }

  public static TimeOffRequestApprovalStatus valueOf(Long id) {
    return Arrays.stream(TimeOffRequestApprovalStatus.values())
        .filter(status -> status.value.equals(id))
        .collect(Collectors.toList()).get(0);
  }

  public static class Converter extends BaseEnumConverter<TimeOffRequestApprovalStatus> {

  }
}
