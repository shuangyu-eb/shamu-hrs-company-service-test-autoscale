package shamu.company.benefit.dto;

import lombok.Data;

@Data
public class EnrollmentBreakdownSearchCondition {
  private static final String NAME = "orderName";
  private static final String PLAN = "plan";
  private static final String COVERAGE = "coverage";
  private static final String COMPANYCOST = "companyCost";
  private static final String EMPLOYEECOST = "employeeCost";

  private String keyword = "";
  private Integer page = 0;
  private Integer size = 20;
  private SortField sortField = SortField.NAME;
  private SortDirection sortDirection = SortDirection.DESC;
  private String planId;
  private String coverageId;

  public String getSortDirection() {
    return sortDirection.name();
  }

  public void setSortDirection(final String sortDirection) {
    this.sortDirection = SortDirection.valueOf(sortDirection);
  }

  public void setSortField(final String sortField) {
    this.sortField = SortField.valueOf(sortField);
  }

  public enum SortField {
    NAME(EnrollmentBreakdownSearchCondition.NAME),
    PLAN(EnrollmentBreakdownSearchCondition.PLAN),
    COVERAGE(EnrollmentBreakdownSearchCondition.COVERAGE),
    COMPANYCOST(EnrollmentBreakdownSearchCondition.COMPANYCOST),
    EMPLOYEECOST(EnrollmentBreakdownSearchCondition.EMPLOYEECOST);

    private final String sortValue;

    SortField(final String sortValue) {
      this.sortValue = sortValue;
    }

    public String getSortValue() {
      return sortValue;
    }
  }

  public enum SortDirection {
    DESC,
    ASC
  }
}
