package shamu.company.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.Country;
import shamu.company.common.service.CountryService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;

@Service
public class UserAddressServiceImpl implements UserAddressService {

  @Autowired
  UserAddressRepository userAddressRepository;

  @Autowired
  StateProvinceService stateProvinceService;

  @Autowired
  CountryService countryService;

  @Autowired
  UserService userService;

  @Override
  public UserAddressDto updateUserAddress(UserAddressDto userAddressDto) {
    String countryName = userAddressDto.getCountryName();
    Country country = countryService.getCountry(countryName);

    Long userId = userAddressDto.getUserId();

    UserAddress userAddress = userAddressDto.getUserAddress(userAddressDto,country);

    UserAddress userAddressUpdated = userAddressRepository.save(userAddress);

    UserAddressDto userAddressDtoUpdated = new UserAddressDto(userAddressUpdated, userId);

    return userAddressDtoUpdated;
  }
}
