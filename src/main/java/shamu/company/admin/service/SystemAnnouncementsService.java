package shamu.company.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.admin.entity.SystemAnnouncement;
import shamu.company.admin.repository.SystemAnnouncementsRepository;


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
}
