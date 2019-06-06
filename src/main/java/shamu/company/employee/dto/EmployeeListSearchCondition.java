package shamu.company.employee.dto;

import lombok.Data;

@Data
public class EmployeeListSearchCondition {

  private static final String LAST_NAME = "lastName";
  private static final String FIRST_NAME = "firstName";
  private static final String DEPARTMENT = "department";
  private static final String JOB_TITLE = "jobTitle";
  private String keyword = "";
  private Integer page = 0;
  private Integer size = 20;
  private SortField sortField = SortField.NAME;
  private SortDirection sortDirection = SortDirection.desc;

  public String getSortDirection() {
    return this.sortDirection.name();
  }

  public void setSortDirection(String sortDirection) {
    this.sortDirection = SortDirection.valueOf(sortDirection);
  }

  public void setSortField(String sortField) {
    this.sortField = SortField.valueOf(sortField);
  }

  public enum SortField {
    NAME(EmployeeListSearchCondition.LAST_NAME),
    DEPARTMENT(EmployeeListSearchCondition.DEPARTMENT),
    JOB_TITLE(EmployeeListSearchCondition.JOB_TITLE);

    private String sortValue;

    SortField(String sortValue) {
      this.sortValue = sortValue;
    }

    public String[] getSortValue() {
      if (EmployeeListSearchCondition.LAST_NAME.equals(this.sortValue)) {
        return new String[]{EmployeeListSearchCondition.LAST_NAME,
            EmployeeListSearchCondition.FIRST_NAME};
      }

      return new String[]{this.sortValue};
    }
  }

  public enum SortDirection {
    desc,
    asc
  }
}
