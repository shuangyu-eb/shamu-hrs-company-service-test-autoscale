package shamu.company.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.repository.OfficeAddressRepository;
import shamu.company.company.entity.OfficeAddress;

public class OfficeAddressServiceTest {

  @Mock private OfficeAddressRepository officeAddressRepository;

  private OfficeAddressService officeAddressService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    officeAddressService = new OfficeAddressService(officeAddressRepository);
  }

  @Test
  void testDelete() {
    final OfficeAddress officeAddress = new OfficeAddress();
    officeAddressService.delete(officeAddress);
    Mockito.verify(officeAddressRepository, Mockito.times(1)).delete(officeAddress);
  }
}
