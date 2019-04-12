package shamu.company.utils;

import shamu.company.user.dto.PersonalInformationDTO;
import shamu.company.user.dto.UserAddressDTO;
import shamu.company.user.dto.UserContactInformationDTO;
import shamu.company.user.dto.UserPersonalInformationDTO;
import org.springframework.beans.BeanUtils;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;

public class PersonalInformationUtil {

    public static PersonalInformationDTO convert(Long userId, UserPersonalInformation personalInfo, UserContactInformation contactInfo, UserAddress userAddress){
        UserAddressDTO userAddressDTO = PersonalInformationUtil.convertUserAddressEntityToDTO(userAddress,userId);
        UserContactInformationDTO userContactInformationDTO = PersonalInformationUtil.convertUserContactInfoEntityToDTO(contactInfo);
        UserPersonalInformationDTO userPersonalInformationDTO = PersonalInformationUtil.convertUserPersonalInfoEntityToDTO(personalInfo);

        PersonalInformationDTO personalInformationDTO = new PersonalInformationDTO(userId,userAddressDTO,userContactInformationDTO,userPersonalInformationDTO);

        return personalInformationDTO;
    }

    public static UserPersonalInformationDTO convertUserPersonalInfoEntityToDTO(UserPersonalInformation source){
        UserPersonalInformationDTO target = new UserPersonalInformationDTO();

        Long genderId = source.getGender().getId();
        String genderName = source.getGender().getName();
        Long maritalStatusId = source.getMaritalStatus().getId();
        String maritalStatusName = source.getMaritalStatus().getName();

        target.setGenderId(genderId);
        target.setGenderName(genderName);
        target.setMaritalStatusId(maritalStatusId);
        target.setMaritalStatusName(maritalStatusName);

        BeanUtils.copyProperties(source,target);
        return target;
    }

    public static UserContactInformationDTO convertUserContactInfoEntityToDTO(UserContactInformation source){
        UserContactInformationDTO target = new UserContactInformationDTO();
        BeanUtils.copyProperties(source,target);
        return target;
    }

    public static UserAddressDTO convertUserAddressEntityToDTO(UserAddress source, Long userId){
        UserAddressDTO target = new UserAddressDTO();

        String countryName = source.getCountry().getName();
        Long stateProvinceId = source.getStateProvince().getId();
        String stateProvinceName = source.getStateProvince().getName();

        target.setCountryName(countryName);
        target.setStateProvinceId(stateProvinceId);
        target.setStateProvinceName(stateProvinceName);
        target.setUserId(userId);

        BeanUtils.copyProperties(source,target);
        return target;
    }
}
