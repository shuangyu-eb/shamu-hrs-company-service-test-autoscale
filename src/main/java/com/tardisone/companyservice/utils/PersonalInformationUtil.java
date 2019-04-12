package com.tardisone.companyservice.utils;

import com.tardisone.companyservice.dto.PersonalInformationDTO;
import com.tardisone.companyservice.dto.UserAddressDTO;
import com.tardisone.companyservice.dto.UserContactInformationDTO;
import com.tardisone.companyservice.dto.UserPersonalInformationDTO;
import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.entity.UserAddress;
import com.tardisone.companyservice.entity.UserContactInformation;
import com.tardisone.companyservice.entity.UserPersonalInformation;
import org.springframework.beans.BeanUtils;

public class PersonalInformationUtil {

    public static PersonalInformationDTO convert(UserAddress userAddress){
        User user = userAddress.getUser();
        Long userId = user.getId();
        UserContactInformation userContactInformation = user.getUserContactInformation();
        UserPersonalInformation userPersonalInformation = user.getUserPersonalInformation();

        UserAddressDTO userAddressDTO = PersonalInformationUtil.convertUserAddressEntityToDTO(userAddress);
        UserContactInformationDTO userContactInformationDTO = PersonalInformationUtil.convertUserContactInfoEntityToDTO(userContactInformation);
        UserPersonalInformationDTO userPersonalInformationDTO = PersonalInformationUtil.convertUserPersonalInfoEntityToDTO(userPersonalInformation);

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

    public static UserAddressDTO convertUserAddressEntityToDTO(UserAddress source){
        UserAddressDTO target = new UserAddressDTO();

        String countryName = source.getCountry().getName();
        Long stateProvinceId = source.getStateProvince().getId();
        String stateProvinceName = source.getStateProvince().getName();
        Long userId = source.getUser().getId();

        target.setCountryName(countryName);
        target.setStateProvinceId(stateProvinceId);
        target.setStateProvinceName(stateProvinceName);
        target.setUserId(userId);

        BeanUtils.copyProperties(source,target);
        return target;
    }
}
