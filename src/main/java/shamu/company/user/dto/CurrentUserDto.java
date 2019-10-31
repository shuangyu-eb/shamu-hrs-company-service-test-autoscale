package shamu.company.user.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import shamu.company.hashids.HashidsFormat;
import shamu.company.hashids.HashidsUtil;
import shamu.company.s3.PreSinged;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrentUserDto {

  @HashidsFormat
  private Long id;

  private String name;

  @PreSinged
  private String imageUrl;

  private List<Long> teamMembers;

  private Boolean verified;

  public List<String> getTeamMembers() {
    if (CollectionUtils.isEmpty(teamMembers)) {
      return Collections.emptyList();
    }

    return teamMembers.stream().map(HashidsUtil::encode).collect(Collectors.toList());
  }
}
