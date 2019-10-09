package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.service.CountryService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.repository.UserAddressRepository;

@Service
public class UserAddressService {

  private final UserAddressRepository userAddressRepository;

  private final CountryService countryService;

  private final StateProvinceService stateProvinceService;

  private final UserAddressMapper userAddressMapper;

  @Autowired
  public UserAddressService(
      final UserAddressRepository userAddressRepository,
      final CountryService countryService,
      final StateProvinceService stateProvinceService,
      final UserAddressMapper userAddressMapper) {
    this.userAddressRepository = userAddressRepository;
    this.countryService = countryService;
    this.stateProvinceService = stateProvinceService;
    this.userAddressMapper = userAddressMapper;
  }

  public UserAddress findUserAddressById(final Long id) {
    return userAddressRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User address does not exist"));
  }

  public UserAddress findUserAddressByUserId(final Long id) {
    return userAddressRepository.findUserAddressByUserId(id);
  }

  public UserAddress save(final UserAddressDto userAddressDto) {
    final Long userId = userAddressDto.getUserId();
    final UserAddress userAddress = findUserAddressByUserId(userId);
    if (userAddress == null) {
      final UserAddress initUserAddress = userAddressMapper
          .createFromUserAddressDto(userAddressDto);
      return userAddressRepository.save(initUserAddress);
    }
    userAddressMapper.updateFromUserAddressDto(userAddress, userAddressDto);
    return updateUserAddress(userAddress);
  }

  private UserAddress updateUserAddress(final UserAddress userAddress) {
    final Country country = userAddress.getCountry();
    final StateProvince stateProvince = userAddress.getStateProvince();

    if (country != null) {
      final Country countryUpdated = countryService.getCountryById(country.getId());
      userAddress.setCountry(countryUpdated);
    }

    if (stateProvince != null) {
      final StateProvince stateProvinceUpdated =
          stateProvinceService.getStateProvinceById(stateProvince.getId());
      userAddress.setStateProvince(stateProvinceUpdated);
    }
    return userAddressRepository.save(userAddress);
  }
}
