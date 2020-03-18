package shamu.company.benefit.dto;

import lombok.Data;

@Data
public class BenefitPlanSearchCondition {
  private static final String NAME = "benefitPlanName";
  private static final String STATUS = "status";
  private static final String DEDUCTIONSBEGIN = "deductionsBegin";
  private static final String DEDUCTIONSEND = "deductionsEnd";

  private String keyword = "";
  private Integer page = 0;
  private Integer size = 20;
  private SortField sortField = SortField.STATUS;
  private SortDirection sortDirection = SortDirection.ASC;
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
    NAME(BenefitPlanSearchCondition.NAME),
    STATUS(BenefitPlanSearchCondition.STATUS),
    DEDUCTIONSBEGIN(BenefitPlanSearchCondition.DEDUCTIONSBEGIN),
    DEDUCTIONSEND(BenefitPlanSearchCondition.DEDUCTIONSEND);

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
