package shamu.company.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@FeignClient(value = "document-service")
@RequestMapping("/server/document")
public interface DocumentClient {

  @PostMapping("/tenants")
  @Async
  void addTenant(final AddTenantDto tenantDto);
}
