package shamu.company.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.user.dto.PersonalInformationDTO;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.PersonalInformationService;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;
import shamu.company.utils.PersonalInformationUtil;

@Service
public class PersonalInformationServiceImpl implements PersonalInformationService {

    @Autowired
    UserService userService;

    @Autowired
    UserAddressService userAddressService;

    @Override
    public PersonalInformationDTO getPersonalInformation(Long userId) {
        User user = userService.getUser(userId);
        UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();
        UserContactInformation userContactInformation = user.getUserContactInformation();
        UserAddress userAddress = userAddressService.findUserAddressByUserId(userId);
        PersonalInformationDTO personalInformationDTO = PersonalInformationUtil.convert(userId,userPersonalInformation,userContactInformation,userAddress);
        return personalInformationDTO;
    }
}
