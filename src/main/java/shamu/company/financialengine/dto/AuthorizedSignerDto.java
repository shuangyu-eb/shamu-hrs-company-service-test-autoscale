package shamu.company.financialengine.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizedSignerDto {

  @JsonAlias({ "employeeUuid" })
  private String employeeId;

  private Boolean willSignCheck;
}
