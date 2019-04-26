package shamu.company.employee.dto;

import lombok.Data;

@Data
public class EmployeeListSearchCondition {

  public enum SortField {
    NAME("userPersonalInformation.firstName"),
    DEPARTMENT("job.department.name"),
    JOB_TITLE("job.title");

    private String sortValue;

    SortField(String sortValue) {
      this.sortValue = sortValue;
    }

    public String getSortValue() {
      return this.sortValue;
    }
  }

  public enum SortDirection {
    desc,
    asc
  }

  private String keyword = "";

  private Integer page = 0;

  private Integer size = 20;

  private SortField sortField = SortField.NAME;

  private SortDirection sortDirection = SortDirection.desc;

  public String getSortDirection() {
    return this.sortDirection.name();
  }

  public void setSortField(String sortField) {
    this.sortField = SortField.valueOf(sortField);
  }

  public void setSortDirection(String sortDirection) {
    this.sortDirection = SortDirection.valueOf(sortDirection);
  }
}
