package shamu.company.employee.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;
import shamu.company.s3.PreSinged;

@Data
@NoArgsConstructor
public class OrgChartDto {

  @HashidsFormat
  private Long id;

  private String firstName;

  private String lastName;

  @PreSinged
  private String imageUrl;

  private String jobTitle;

  private String city;

  private String state;

  private String department;

  @HashidsFormat
  private Long managerId;

  private Integer directReportsCount;

  private List<OrgChartDto> directReports = new ArrayList<>();
}
