package liquibase.ext;

import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.lockservice.StandardLockService;

public class ForceReleaseLockService extends StandardLockService {

  @Override
  public int getPriority() {
    return super.getPriority() + 1;
  }

  @Override
  public void waitForLock() throws LockException {
    try {
      super.forceReleaseLock();
    } catch (final DatabaseException e) {
      throw new LockException("Unable to forcibly release the database lock.", e);
    }

    super.waitForLock();
  }
}
