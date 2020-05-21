package shamu.company.client;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import shamu.company.server.dto.CompanyUser;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddTenantDto {

  private CompanyUser user;

  private PactsafeCompanyDto company;
}
