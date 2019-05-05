package shamu.company.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.service.CountryService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.service.UserAddressService;

@Service
public class UserAddressServiceImpl implements UserAddressService {

  private final UserAddressRepository userAddressRepository;

  private final CountryService countryService;

  private final StateProvinceService stateProvinceService;

  @Autowired
  public UserAddressServiceImpl(
      UserAddressRepository userAddressRepository,
      CountryService countryService,
      StateProvinceService stateProvinceService) {
    this.userAddressRepository = userAddressRepository;
    this.countryService = countryService;
    this.stateProvinceService = stateProvinceService;
  }

  @Override
  public UserAddress updateUserAddress(UserAddress userAddress) {
    Country country = userAddress.getCountry();
    StateProvince stateProvince = userAddress.getStateProvince();

    if (country != null) {
      Country countryUpdated = countryService.getCountryById(country.getId());
      userAddress.setCountry(countryUpdated);
    }

    if (stateProvince != null) {
      StateProvince stateProvinceUpdated =
          stateProvinceService.getStateProvinceById(stateProvince.getId());
      userAddress.setStateProvince(stateProvinceUpdated);
    }
    return userAddressRepository.save(userAddress);
  }

  @Override
  public UserAddress findUserAddressById(Long id) {
    return userAddressRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User address does not exist"));
  }

  @Override
  public UserAddress findUserAddressByUserId(Long id) {
    return userAddressRepository
        .findUserAddressByUserId(id)
        .orElseThrow(() -> new ResourceNotFoundException("User address does not exist"));
  }
}
