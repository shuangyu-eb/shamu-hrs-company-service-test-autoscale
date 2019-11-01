package shamu.company.admin.dto;

import java.util.List;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class MockUserDto {

  @HashidsFormat
  private Long id;

  private String email;

  private String imageUrl;

  private Long companyId;

  private List<String> permissions;
}
