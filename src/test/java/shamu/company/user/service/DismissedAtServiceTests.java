package shamu.company.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.user.entity.DismissedAt;
import shamu.company.user.repository.DismissedAtRepository;
import shamu.company.utils.UuidUtil;

class DismissedAtServiceTests {

  @InjectMocks private DismissedAtService dismissedAtService;

  @Mock private DismissedAtRepository dismissedAtRepository;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void findByUserIdAndSystemAnnouncementId() {
    final DismissedAt dismissedAt = new DismissedAt();
    dismissedAt.setId("1");

    Mockito.when(
            dismissedAtRepository.findByUserIdAndSystemAnnouncementId(Mockito.any(), Mockito.any()))
        .thenReturn(dismissedAt);

    Assertions.assertDoesNotThrow(
        () ->
            dismissedAtService.findByUserIdAndSystemAnnouncementId(UuidUtil.getUuidString(), "1"));
    Assertions.assertEquals(
        dismissedAtService
            .findByUserIdAndSystemAnnouncementId(Mockito.any(), Mockito.any())
            .getId(),
        dismissedAt.getId());
  }

  @Test
  void save() {
    final DismissedAt dismissedAt = new DismissedAt();
    dismissedAt.setId("1");

    Mockito.when(dismissedAtRepository.save(Mockito.any())).thenReturn(dismissedAt);

    Assertions.assertDoesNotThrow(() -> dismissedAtService.save(dismissedAt));
    Assertions.assertEquals(dismissedAtService.save(dismissedAt).getId(), dismissedAt.getId());
  }
}
