package shamu.company.common.multitenant;

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
}
