package shamu.company.financialengine.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyAddressesDto {

  private String uuid;

  @JsonAlias("companyAddressType")
  private String type;

}
