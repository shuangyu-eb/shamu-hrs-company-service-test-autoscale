package shamu.company.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.repository.OfficeAddressRepository;
import shamu.company.company.entity.OfficeAddress;

@Service
@Transactional
public class OfficeAddressService {

  private final OfficeAddressRepository officeAddressRepository;

  @Autowired
  public OfficeAddressService(final OfficeAddressRepository officeAddressRepository) {
    this.officeAddressRepository = officeAddressRepository;
  }

  public void delete(OfficeAddress officeAddress) {
    officeAddressRepository.delete(officeAddress);
  }
}
