package shamu.company.user.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrentUserDto {

  private String id;

  private String name;

  private String imageUrl;

  private List<String> teamMembers;

  private Boolean verified;
}
