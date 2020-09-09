package shamu.company.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@FeignClient(value = "document-service")
@RequestMapping("/server/document")
public interface DocumentClient {

  @DeleteMapping("/request-user/{userId}")
  void deleteDocumentRequestUser(@PathVariable final String userId);
}
