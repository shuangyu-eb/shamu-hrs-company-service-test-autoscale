package shamu.company.user.service.impl;

import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.user.dto.PersonalInformationDTO;
import shamu.company.user.dto.UserAddressDTO;
import shamu.company.common.service.CountryService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;
import shamu.company.utils.PersonalInformationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public UserAddressDTO updateUserAddress(UserAddressDTO userAddressDTO) {
        String city = userAddressDTO.getCity();

        String countryName = userAddressDTO.getCountryName();
        Country country = countryService.getCountry(countryName);

        Long userId = userAddressDTO.getUserId();
        User user = new User();
        user.setId(userId);


        Long stateProvinceId = userAddressDTO.getStateProvinceId();
        StateProvince stateProvince = stateProvinceService.getStateProvince(stateProvinceId);


        String postalCode = userAddressDTO.getPostalCode();
        String street1 = userAddressDTO.getStreet1();
        String street2 = userAddressDTO.getStreet2();
        Long id = userAddressDTO.getId();

        UserAddress userAddress = new UserAddress(user,street1,street2,city,stateProvince,country,postalCode);
        userAddress.setId(id);

        UserAddress userAddressUpdated = userAddressRepository.save(userAddress);

        UserAddressDTO userAddressDTOUpdated = PersonalInformationUtil.convertUserAddressEntityToDTO(userAddressUpdated,userId);

        return userAddressDTOUpdated;
    }

    @Override
    public UserAddress findUserAddressByUserId(Long userId) {
        return userAddressRepository.findUserAddressByUserId(userId);
    }
}
