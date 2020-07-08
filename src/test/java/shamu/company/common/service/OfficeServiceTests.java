package shamu.company.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.company.entity.Office;
import shamu.company.utils.UuidUtil;

public class OfficeServiceTests {

  @Mock private OfficeRepository officeRepository;

  private OfficeService officeService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    officeService = new OfficeService(officeRepository);
  }

  @Nested
  class testFindById {
    @Test
    void whenIdExists_thenShouldSuccess() {
      final Optional<Office> optional = Optional.of(new Office());
      Mockito.when(officeRepository.findById(Mockito.anyString())).thenReturn(optional);
      assertThatCode(() -> officeService.findById("1")).doesNotThrowAnyException();
    }

    @Test
    void whenIdNotExists_thenShouldThrowException() {
      final Optional<Office> optional = Optional.empty();
      Mockito.when(officeRepository.findById(Mockito.anyString())).thenReturn(optional);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> officeService.findById("1"));
    }

    @Test
    void testFindByCompanyId() {
      officeService.findByCompanyId("1");
      Mockito.verify(officeRepository, Mockito.times(1)).findByCompanyId(Mockito.anyString());
    }

    @Test
    void testSave() {
      final Office office = new Office();
      officeService.save(office);
      Mockito.verify(officeRepository, Mockito.times(1)).save(office);
    }

    @Test
    void testFindCountByOffice() {
      officeService.findCountByOffice("1");
      Mockito.verify(officeRepository, Mockito.times(1)).findCountByOffice(Mockito.anyString());
    }

    @Test
    void testDelete() {
      officeService.delete("1");
      Mockito.verify(officeRepository, Mockito.times(1)).delete(Mockito.anyString());
    }
  }

  @Test
  void findByNameAndCompanyId() {
    final List<Office> offices = new ArrayList<>();
    final Office office = new Office();
    office.setId(UuidUtil.getUuidString());

    offices.add(office);

    Mockito.when(officeRepository.findByNameAndCompanyId("123", "123")).thenReturn(offices);

    assertThat(officeService.findByNameAndCompanyId("123", "123")).isEqualTo(offices);
  }
}
