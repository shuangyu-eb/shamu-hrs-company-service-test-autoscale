package shamu.company.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import shamu.company.admin.dto.SystemAnnouncementDto;
import shamu.company.admin.entity.SystemAnnouncement;
import shamu.company.admin.repository.SystemAnnouncementsRepository;
import shamu.company.common.exception.NotFoundException;

@Service
public class SystemAnnouncementsService {

  private SystemAnnouncementsRepository systemAnnouncementsRepository;

  @Autowired
  public SystemAnnouncementsService(
      final SystemAnnouncementsRepository systemAnnouncementsRepository) {
    this.systemAnnouncementsRepository = systemAnnouncementsRepository;
  }

  public SystemAnnouncement getSystemActiveAnnouncement() {
    return systemAnnouncementsRepository.getSystemActiveAnnouncement();
  }

  public SystemAnnouncement save(final SystemAnnouncement systemAnnouncement) {
    return systemAnnouncementsRepository.save(systemAnnouncement);
  }

  public SystemAnnouncement findById(final String id) {
    return systemAnnouncementsRepository
        .findById(id)
        .orElseThrow(
            () ->
                new NotFoundException(
                    String.format("System Announcement with id %s not found!", id),
                    id,
                    "system announcement"));
  }

  public Page<SystemAnnouncementDto> getSystemPastAnnouncements(final Pageable pageable) {
    return systemAnnouncementsRepository.getSystemPastAnnouncements(pageable);
  }
}
