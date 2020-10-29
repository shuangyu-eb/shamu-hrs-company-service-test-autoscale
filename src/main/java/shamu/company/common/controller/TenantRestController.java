package shamu.company.common.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.database.LiquibaseManager;
import shamu.company.common.entity.Tenant;
import shamu.company.common.exception.errormapping.ForbiddenException;
import shamu.company.common.exception.errormapping.LiquibaseExecuteFailedException;
import shamu.company.helpers.dynamodb.DynamoDBManager;


@Profile("!local")
@RestApiController
public class TenantRestController {

  private final LiquibaseManager liquibaseManager;

  private final String lambdaToken;

  private final DynamoDBManager dynamoDBManager;

  private static final String TENANT_LAMBDA_TOKEN_HEADER = "X-Tenant-Lambda-Token";

  public TenantRestController(
      final LiquibaseManager liquibaseManager,
      final DynamoDBManager dynamoDBManager,
      @Value("${aws.lambda.tenant-endpoint-token}") final String lambdaToken) {
    this.liquibaseManager = liquibaseManager;
    this.lambdaToken = lambdaToken;
    this.dynamoDBManager = dynamoDBManager;
  }

  @PostMapping("/tenants/{id}")
  public ResponseEntity tenantPreprovisioning(
      final HttpServletRequest request, @Valid @NotBlank @PathVariable final String id) {
    final String token = request.getHeader(TENANT_LAMBDA_TOKEN_HEADER);
    if (!lambdaToken.equals(token)) {
      throw new ForbiddenException("Forbidden");
    }
    final String companyId = id.toUpperCase();
    try {
      liquibaseManager.initSchema(Tenant.builder().companyId(companyId).build());
    } catch (final LiquibaseExecuteFailedException e) {
      dynamoDBManager.deleteDynamoRecord(companyId);
      throw e;
    }
    dynamoDBManager.updateDynamoRecord(companyId);
    return new ResponseEntity(HttpStatus.OK);
  }
}
