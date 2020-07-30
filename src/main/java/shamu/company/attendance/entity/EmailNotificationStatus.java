package shamu.company.attendance.entity;

public enum EmailNotificationStatus {
  ON(1),
  OFF(0);

  private final int value;

  EmailNotificationStatus(final int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
