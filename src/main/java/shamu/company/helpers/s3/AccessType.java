package shamu.company.helpers.s3;

import com.amazonaws.services.s3.model.CannedAccessControlList;

public enum AccessType {

  PublicRead(CannedAccessControlList.PublicRead),

  Private(CannedAccessControlList.Private);

  private final CannedAccessControlList accessLevel;

  AccessType(final CannedAccessControlList accessLevel) {
    this.accessLevel = accessLevel;
  }

  public CannedAccessControlList getAccessLevel() {
    return accessLevel;
  }
}
