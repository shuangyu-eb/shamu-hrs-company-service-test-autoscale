package shamu.company.common.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.database.LiquibaseManager;
import shamu.company.common.entity.Tenant;
import shamu.company.common.exception.errormapping.ForbiddenException;
import shamu.company.utils.UuidUtil;

@RestApiController
public class TenantRestController {

  private final LiquibaseManager liquibaseManager;

  private final String lambdaToken;

  private static final String TENANT_LAMBDA_TOKEN_HEADER = "X-Tenant-Lambda-Token";

  public TenantRestController(
      final LiquibaseManager liquibaseManager, @Value("${lambda.token}") final String lambdaToken) {
    this.liquibaseManager = liquibaseManager;
    this.lambdaToken = lambdaToken;
  }

  @PostMapping("/tenant")
  public String tenantPreprovisioning(final HttpServletRequest request) {
    final String token = request.getHeader(TENANT_LAMBDA_TOKEN_HEADER);
    if (!lambdaToken.equals(token)) {
      throw new ForbiddenException("Forbidden");
    }
    final String companyId = UuidUtil.getUuidString().toUpperCase();
    liquibaseManager.initSchema(Tenant.builder().companyId(companyId).build());
    return companyId;
  }
}
