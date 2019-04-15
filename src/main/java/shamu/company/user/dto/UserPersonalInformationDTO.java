package shamu.company.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import shamu.company.user.entity.UserPersonalInformation;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserPersonalInformationDTO {
    private Long id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String preferredName;

    private Timestamp birthDate;

    private String ssn;

    private Long genderId;

    private String genderName;

    private Long maritalStatusId;

    private String maritalStatusName;

    public UserPersonalInformationDTO(UserPersonalInformation userPersonalInformation) {
        Long genderId = userPersonalInformation.getGender().getId();
        String genderName = userPersonalInformation.getGender().getName();
        Long maritalStatusId = userPersonalInformation.getMaritalStatus().getId();
        String maritalStatusName = userPersonalInformation.getMaritalStatus().getName();

        this.setGenderId(genderId);
        this.setGenderName(genderName);
        this.setMaritalStatusId(maritalStatusId);
        this.setMaritalStatusName(maritalStatusName);

        BeanUtils.copyProperties(userPersonalInformation,this);
    }

}
