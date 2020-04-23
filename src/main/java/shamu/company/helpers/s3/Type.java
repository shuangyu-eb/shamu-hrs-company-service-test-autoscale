package shamu.company.helpers.s3;

public enum Type {
  TEMP("temp"),
  IMAGE("image"),
  DEFAULT("uploads");

  private final String folder;

  Type(final String folder) {
    this.folder = folder;
  }

  public String getFolder() {
    return folder;
  }
}
