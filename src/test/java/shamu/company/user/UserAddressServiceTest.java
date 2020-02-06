package shamu.company.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.service.CountryService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.service.UserAddressService;

import java.util.UUID;

public class UserAddressServiceTest {
  @Mock
  private UserAddressRepository userAddressRepository;

  @Mock
  private CountryService countryService;

  @Mock
  private StateProvinceService stateProvinceService;

  @Mock
  private UserAddressMapper userAddressMapper;

  @InjectMocks
  private UserAddressService userAddressService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class UserAddress {
    private String userId;
    private UserAddressDto userAddressDto;

    @BeforeEach
    void init() {
      userId = UUID.randomUUID().toString().replaceAll("-", "");
      userAddressDto = new UserAddressDto();
      userAddressDto.setUserId(userId);
    }

    @Test
    void whenNotExist_thenShouldCreate() {
      Mockito.when(userAddressRepository.findUserAddressByUserId(userId)).thenReturn(null);
      userAddressService.save(userAddressDto);
      Mockito.verify(userAddressMapper, Mockito.times(1))
          .createFromUserAddressDto(userAddressDto);
    }

    @Test
    void whenExist_thenShouldUpdate() {
      final shamu.company.user.entity.UserAddress userAddress = new shamu.company.user.entity.UserAddress();
      userAddress.setCountry(new Country(UUID.randomUUID().toString().replaceAll("-", "")));
      userAddress.setStateProvince(new StateProvince(UUID.randomUUID().toString().replaceAll("-", "")));
      Mockito.when(userAddressRepository.findUserAddressByUserId(userId)).thenReturn(userAddress);
      userAddressService.save(userAddressDto);
      Mockito.verify(userAddressMapper, Mockito.times(1))
          .updateFromUserAddressDto(userAddress, userAddressDto);
    }
  }
}
