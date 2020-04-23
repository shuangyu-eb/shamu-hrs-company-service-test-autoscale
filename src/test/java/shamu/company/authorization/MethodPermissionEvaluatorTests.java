package shamu.company.authorization;

import java.util.ArrayList;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.authorization.Permission.Name;

class MethodPermissionEvaluatorTests {

  @Mock private PermissionUtils permissionUtils;

  @InjectMocks private MethodPermissionEvaluator methodPermissionEvaluator;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class HasPermission {

    @Test
    void testWhenTargetIsStringType() {

      methodPermissionEvaluator.hasPermission(
          null, RandomStringUtils.randomAlphabetic(16), Type.USER.name(), Name.SUPER_PERMISSION);
      Mockito.verify(permissionUtils, Mockito.times(1))
          .hasPermission(Mockito.any(), Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void testWhenTargetIsListType() {

      methodPermissionEvaluator.hasPermission(
          null, new ArrayList<>(), Type.USER.name(), Name.SUPER_PERMISSION);
      Mockito.verify(permissionUtils, Mockito.times(1))
          .hasPermission(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.any());
    }
  }
}
