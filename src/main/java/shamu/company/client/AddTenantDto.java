package shamu.company.client;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddTenantDto {

  private PactsafeUserDto user;

  private PactsafeCompanyDto company;
}
