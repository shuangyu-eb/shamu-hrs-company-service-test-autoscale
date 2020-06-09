package shamu.company.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import shamu.company.admin.entity.SystemAnnouncement;
import shamu.company.admin.repository.SystemAnnouncementsRepository;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;

class SystemAnnouncementsServiceTests {

  @InjectMocks private SystemAnnouncementsService systemAnnouncementsService;

  @Mock private SystemAnnouncementsRepository systemAnnouncementsRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void getSystemActiveAnnouncement() {
    final SystemAnnouncement systemAnnouncement = new SystemAnnouncement();
    systemAnnouncement.setId("1");

    Mockito.when(systemAnnouncementsRepository.getSystemActiveAnnouncement())
        .thenReturn(systemAnnouncement);

    assertThat(systemAnnouncementsService.getSystemActiveAnnouncement().getId())
        .isEqualTo(systemAnnouncement.getId());
  }

  @Test
  void save() {
    final SystemAnnouncement systemAnnouncement = new SystemAnnouncement();
    systemAnnouncement.setId("1");

    Mockito.when(systemAnnouncementsRepository.save(Mockito.any())).thenReturn(systemAnnouncement);

    assertThat(systemAnnouncementsService.save(systemAnnouncement).getId())
        .isEqualTo(systemAnnouncement.getId());
  }

  @Test
  void testGetSystemPastAnnouncements() {
    Mockito.when(systemAnnouncementsRepository.getSystemPastAnnouncements(Mockito.any()))
        .thenReturn(Page.empty());

    assertThatCode(
        () -> {
          systemAnnouncementsService.getSystemPastAnnouncements(Mockito.any());
        });
  }

  @Nested
  class testFindById {

    @Test
    void whenSystemAnnouncementIsNotNull_thenShouldSuccess() {
      final SystemAnnouncement systemAnnouncement = new SystemAnnouncement();
      systemAnnouncement.setId("1");

      Mockito.when(systemAnnouncementsRepository.findById(Mockito.any()))
          .thenReturn(Optional.of(systemAnnouncement));

      assertThat(systemAnnouncementsService.findById("1").getId())
          .isEqualTo(systemAnnouncement.getId());
    }

    @Test
    void whenSystemAnnouncementIsNull_thenShouldThrows() {
      final SystemAnnouncement systemAnnouncement = new SystemAnnouncement();
      systemAnnouncement.setId("1");

      Mockito.when(systemAnnouncementsRepository.findById(Mockito.any()))
          .thenReturn(Optional.empty());

      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(
              () -> {
                systemAnnouncementsService.findById("1");
              });
    }
  }
}
