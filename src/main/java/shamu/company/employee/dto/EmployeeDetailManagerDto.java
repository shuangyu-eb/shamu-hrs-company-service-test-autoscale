package shamu.company.employee.dto;

import lombok.Data;

@Data
public class EmployeeDetailManagerDto {
  private String userId;

  private String firstName;

  private String lastName;

  private String preferredName;

  private String imageUrl;

  private String jobTitle;
}
