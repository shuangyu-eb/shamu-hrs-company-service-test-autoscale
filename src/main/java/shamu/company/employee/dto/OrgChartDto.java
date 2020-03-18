package shamu.company.employee.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrgChartDto {

  private String id;

  private String firstName;

  private String preferredName;

  private String lastName;

  private String imageUrl;

  private String jobTitle;

  private String city;

  private String state;

  private String department;

  private String managerId;

  private Integer directReportsCount;

  private List<OrgChartDto> directReports = new ArrayList<>();

  private Boolean isCompany = false;
}
