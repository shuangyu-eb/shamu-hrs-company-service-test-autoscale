package shamu.company.common.multitenant;

import shamu.company.common.Hook;

public class TenantContext {

  private TenantContext() {}

  private static final ThreadLocal<String> currentTenant = new InheritableThreadLocal<>();

  public static String getCurrentTenant() {
    return currentTenant.get();
  }

  public static void setCurrentTenant(final String tenant) {
    currentTenant.set(tenant);
  }

  public static void clear() {
    currentTenant.remove();
  }

  public static void withInTenant(final String tenant, final Hook hook) {
    final String lastTenant = getCurrentTenant();
    setCurrentTenant(tenant);
    hook.callback();
    setCurrentTenant(lastTenant);
  }
}
