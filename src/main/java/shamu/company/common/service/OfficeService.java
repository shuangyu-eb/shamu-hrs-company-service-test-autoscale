package shamu.company.common.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.company.entity.Office;

@Service
@Transactional
public class OfficeService {

  private final OfficeRepository officeRepository;

  @Autowired
  public OfficeService(final OfficeRepository officeRepository) {
    this.officeRepository = officeRepository;
  }

  public Office findById(final String id) {
    return officeRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException(String.format("Office with id %s not found!", id)));
  }

  public List<Office> findByCompanyId(String companyId) {
    return officeRepository.findByCompanyId(companyId);
  }

  public Integer findCountByOffice(String officeId) {
    return officeRepository.findCountByOffice(officeId);
  }

  public Office save(Office office) {
    return officeRepository.save(office);
  }

  public void delete(String id) {
    officeRepository.delete(id);
  }
}
