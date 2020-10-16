package shamu.company.job.dto;

public enum JobSelectOptionUpdateField {
  DEPARTMENT("Department"),
  JOB_TITLE("Job Title"),
  WORK_ADDRESS("Work Address");

  private final String value;

  JobSelectOptionUpdateField(final String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
