package shamu.company.common.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import shamu.company.common.entity.Tenant;

@AllArgsConstructor
public class TenantCreatedEvent {

  @Getter private final Tenant tenant;
}
