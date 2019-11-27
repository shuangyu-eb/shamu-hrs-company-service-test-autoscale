package shamu.company.job.dto;

public enum JobSelectOptionUpdateField {
  DEPARTMENT("Department"),
  JOB_TITLE("Job Title"),
  EMPLOYMENT_TYPE("Employment Type"),
  OFFICE_LOCATION("Office Location");

  private final String value;

  JobSelectOptionUpdateField(final String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

