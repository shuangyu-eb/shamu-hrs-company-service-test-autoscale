package shamu.company.admin.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.admin.entity.SystemAnnouncement;
import shamu.company.admin.repository.SystemAnnouncementsRepository;
import shamu.company.user.entity.User;

import static org.junit.jupiter.api.Assertions.*;

class SystemAnnouncementsServiceTests {

  @InjectMocks
  private SystemAnnouncementsService systemAnnouncementsService;

  @Mock
  private SystemAnnouncementsRepository systemAnnouncementsRepository;

    @BeforeEach
    void setUp() {
      MockitoAnnotations.initMocks(this);
    }

    @Test
    void getSystemActiveAnnouncement() {
      final SystemAnnouncement systemAnnouncement = new SystemAnnouncement();
      systemAnnouncement.setId("1");

      Mockito.when(systemAnnouncementsRepository.getSystemActiveAnnouncement()).thenReturn(systemAnnouncement);

      Assertions.assertEquals(systemAnnouncementsService.getSystemActiveAnnouncement().getId(), systemAnnouncement.getId());
      Assertions.assertDoesNotThrow(() -> systemAnnouncementsService.getSystemActiveAnnouncement());
    }

    @Test
    void save() {
      final SystemAnnouncement systemAnnouncement = new SystemAnnouncement();
      systemAnnouncement.setId("1");

      Mockito.when(systemAnnouncementsRepository.save(Mockito.any())).thenReturn(systemAnnouncement);

      Assertions.assertEquals(systemAnnouncementsService.save(systemAnnouncement).getId(), systemAnnouncement.getId());
      Assertions.assertDoesNotThrow(() -> systemAnnouncementsService.save(systemAnnouncement));
    }
}
