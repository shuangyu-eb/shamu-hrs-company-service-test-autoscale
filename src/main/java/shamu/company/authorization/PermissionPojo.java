package shamu.company.authorization;

import com.alibaba.fastjson.annotation.JSONField;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import shamu.company.hashids.HashidsFormat;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class PermissionPojo implements GrantedAuthority {

  private String name;

  @HashidsFormat
  private List<Long> ids;

  @Override
  @JSONField(serialize = false)
  public String getAuthority() {
    return this.name;
  }

  PermissionPojo(String name) {
    this.name = name;
  }
}
