package shamu.company.admin.dto;

import java.util.List;
import lombok.Data;

@Data
public class MockUserDto {

  private String id;

  private String email;

  private String imageUrl;

  private String companyId;

  private List<String> permissions;
}
