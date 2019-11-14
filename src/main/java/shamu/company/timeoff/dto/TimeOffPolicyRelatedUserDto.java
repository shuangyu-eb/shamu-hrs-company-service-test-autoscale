package shamu.company.timeoff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeOffPolicyRelatedUserDto {

  private String firstName;

  private String id;

  private String imageUrl;

  private String jobTitle;

  private String lastName;

  private Integer balance;
}
