package shamu.company.user.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.user.dto.BasicInformationDTO;
import shamu.company.user.dto.PersonalInformationDTO;
import shamu.company.user.dto.UserAddressDTO;
import shamu.company.user.dto.UserPersonalInformationDTO;
import shamu.company.user.entity.*;
import shamu.company.user.service.PersonalInformationService;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.user.service.UserService;
import shamu.company.utils.PersonalInformationUtil;

@Service
public class PersonalInformationServiceImpl implements PersonalInformationService {

    @Autowired
    UserService userService;

    @Autowired
    UserAddressService userAddressService;

    @Autowired
    UserPersonalInformationService userPersonalInformationService;

    @Override
    public PersonalInformationDTO getPersonalInformation(Long userId) {
        User user = userService.getUser(userId);
        UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();
        UserContactInformation userContactInformation = user.getUserContactInformation();
        UserAddress userAddress = userAddressService.findUserAddressByUserId(userId);
        PersonalInformationDTO personalInformationDTO = PersonalInformationUtil.convert(userId,userPersonalInformation,userContactInformation,userAddress);
        return personalInformationDTO;
    }

    @Override
    public BasicInformationDTO updateBasicInformation(BasicInformationDTO basicInformationDTO) {
        UserPersonalInformationDTO userPersonalInformationDTO = basicInformationDTO.getUserPersonalInformation();
        UserAddressDTO userAddressDTO = basicInformationDTO.getUserAddress();

        UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
        BeanUtils.copyProperties(userPersonalInformationDTO,userPersonalInformation);

        Gender gender = new Gender();
        gender.setId(userPersonalInformationDTO.getGenderId());
        userPersonalInformation.setGender(gender);

        MaritalStatus maritalStatus = new MaritalStatus();
        maritalStatus.setId(userPersonalInformationDTO.getMaritalStatusId());
        userPersonalInformation.setMaritalStatus(maritalStatus);

        UserPersonalInformationDTO userPersonalInformationDTOUpdated = userPersonalInformationService.update(userPersonalInformation);
        UserAddressDTO userAddressDTOUpdated = userAddressService.updateUserAddress(userAddressDTO);

        BasicInformationDTO basicInformationDTOUpdated = new BasicInformationDTO(userAddressDTOUpdated,userPersonalInformationDTOUpdated);


        return basicInformationDTOUpdated;
    }
}
