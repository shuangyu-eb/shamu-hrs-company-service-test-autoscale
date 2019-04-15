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

  @Autowired UserAddressRepository userAddressRepository;

  @Autowired StateProvinceService stateProvinceService;

  @Autowired CountryService countryService;

  @Autowired UserService userService;

  @Override
  public UserAddressDto updateUserAddress(UserAddressDto userAddressDTO) {
    String city = userAddressDTO.getCity();

    String countryName = userAddressDTO.getCountryName();
    Country country = countryService.getCountry(countryName);

    Long userId = userAddressDTO.getUserId();
    Long stateProvinceId = userAddressDTO.getStateProvinceId();

    String postalCode = userAddressDTO.getPostalCode();
    String street1 = userAddressDTO.getStreet1();
    String street2 = userAddressDTO.getStreet2();
    Long id = userAddressDTO.getId();

    UserAddress userAddress =
        new UserAddress(id, userId, street1, street2, city, stateProvinceId, country, postalCode);

    UserAddress userAddressUpdated = userAddressRepository.save(userAddress);

    UserAddressDto userAddressDtoUpdated = new UserAddressDto(userAddressUpdated, userId);

    return userAddressDtoUpdated;
  }
}
